// view/BoardPanel.java
package swing;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BoardPanel extends JPanel {

    /* ─── 필드 ─── */
    private final Main  frame;
    private final long  userId;
    private final boolean  isAdmin;
    private static final DateTimeFormatter ISO_IN  = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter OUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    private final Gson gson = new Gson();

    /** JTable ← 여기에 표시할 데이터 컬렉션 */
    private final List<PostDto> posts = new ArrayList<>();
    private final PostTableModel tableModel = new PostTableModel();
    private final JTable         table      = new JTable(tableModel);

    /* ─── 생성자 ─── */
    public BoardPanel(Main frame, long userId, boolean isAdmin) {
        this.frame = frame;
        this.userId = userId;
        this.isAdmin = isAdmin;
        setLayout(new BorderLayout(8, 8));

        /* 상단 툴바: 새로고침 · 로그아웃 */
        JPanel topBar   = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton refresh = new JButton("새로고침");
        JButton logout  = new JButton("로그아웃");
        topBar.add(refresh); topBar.add(logout);
        add(topBar, BorderLayout.NORTH);

        /* 게시글 목록 JTable */
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setMaxWidth(60);   // 번호 열 폭 고정
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 더블-클릭하면 상세 보기
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    detail(posts.get(table.getSelectedRow()));
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        /* 하단 글쓰기 버튼 */
        JButton writeBtn = new JButton("글쓰기");
        add(writeBtn, BorderLayout.SOUTH);

        /* 이벤트 */
        refresh.addActionListener(e -> reload());
        logout .addActionListener(e -> logout());
        writeBtn.addActionListener(e -> write());

        reload();   // 첫 로딩
    }
    
    /* ───── 글 수정 ───── */
    private void edit(PostDto p) {
        JTextField title = new JTextField(p.title());
        JTextArea  body  = new JTextArea(p.content(), 10, 30);

        int ok = JOptionPane.showConfirmDialog(this,
                new Object[]{"제목", title, "내용", new JScrollPane(body)},
                "글 수정", JOptionPane.OK_CANCEL_OPTION);

        if (ok != JOptionPane.OK_OPTION) return;

        try {
            ApiClient.put("/api/posts/" + p.id(),
                    Map.of("title", title.getText(), "content", body.getText()));
            JOptionPane.showMessageDialog(this, "수정 완료!");
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "수정 실패: " + ex.getMessage());
        }
    }

    /* ───── 글 삭제 ───── */
    private void remove(PostDto p) {
        int res = JOptionPane.showConfirmDialog(this,
                "정말 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;

        try {
            ApiClient.delete("/api/posts/" + p.id());
            JOptionPane.showMessageDialog(this, "삭제 완료!");
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "삭제 실패: " + ex.getMessage());
        }
    }

    
    /* ───────────────── 다중 줄 표시용 렌더러 ───────────────── */
    private class MultilineCellRenderer extends JTextArea implements TableCellRenderer {

        MultilineCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setBorder(null);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText(value == null ? "" : value.toString());

            /* 선택 상태 색상 유지 */
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            /* 폭 고정 → 줄바꿈 후 높이 계산 */
            int colWidth = table.getColumnModel().getColumn(column).getWidth();
            setSize(colWidth, Short.MAX_VALUE);
            int prefH = getPreferredSize().height;
            if (table.getRowHeight(row) != prefH) {
                table.setRowHeight(row, prefH);
            }
            return this;
        }
    }

    /* ───────────────── 로그아웃 ───────────────── */
    private void logout() {
        try {
            // ApiClient.post("/api/users/logout?userId=" + userId, Map.of());
        } catch (Exception ignore) {}
        JOptionPane.showMessageDialog(this, "로그아웃되었습니다.");
        frame.showLogin();
    }

    /* ───────────────── 목록 갱신 ───────────────── */
    private void reload() {
        try {
            Type t = TypeToken.getParameterized(List.class, PostDto.class).getType();
            List<PostDto> fetched = gson.fromJson(ApiClient.get("/api/posts"), t);

            posts.clear();
            posts.addAll(fetched);

            // 작성일(문자열) 내림차순 정렬 → 최신 글이 index 0
            posts.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
            // ※ createdAt 이 LocalDateTime 타입이면 b.getCreatedAt().compareTo(a.getCreatedAt()) 로

            tableModel.fireTableDataChanged();   // JTable 새로고침
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "목록 로딩 실패: " + ex.getMessage());
        }
    }

    /* ───────────────── 글쓰기 ───────────────── */
    private void write() {
        JTextField title = new JTextField();
        JTextArea  body  = new JTextArea(10, 30);
        int ok = JOptionPane.showConfirmDialog(this,
                new Object[]{"제목", title, "내용", new JScrollPane(body)},
                "새 글 작성", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            ApiClient.post("/api/posts?userId=" + userId,
                    Map.of("title", title.getText(), "content", body.getText()));
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "글쓰기 실패: " + ex.getMessage());
        }
    }

    /* ───────────────── 상세 보기 & 댓글 ───────────────── */
    private void detail(PostDto p) {
        try {
            /* 1) 글 + 댓글 데이터 가져오기 */
            PostDto post = gson.fromJson(ApiClient.get("/api/posts/" + p.id()), PostDto.class);
            Type t = TypeToken.getParameterized(List.class, CommentDto.class).getType();
            List<CommentDto> comments = gson.fromJson(
                    ApiClient.get("/api/posts/" + p.id() + "/comments"), t);

            /* 2) 다이얼로그 기본 설정 */
            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                                      "게시글 상세", true);
            dlg.setLayout(new BorderLayout(10, 10));

            /* ─── (A) 상단: 제목 + 메타정보 ─── */
            JPanel header = new JPanel(new BorderLayout());
            JLabel titleLbl = new JLabel(post.title());
            titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 18f));
            header.add(titleLbl, BorderLayout.NORTH);
            String formattedDate = LocalDateTime
                    .parse(post.createdAt(), ISO_IN)   // String → LocalDateTime
                    .format(OUT_FMT);                  // 원하는 형식으로 변환
            JLabel metaLbl = new JLabel("작성자: " + post.author() +
                                        "   |   작성일: " +  formattedDate);
            metaLbl.setFont(metaLbl.getFont().deriveFont(12f));
            header.add(metaLbl, BorderLayout.SOUTH);
            dlg.add(header, BorderLayout.NORTH);

            /* ─── (B) 본문 내용 ─── */
            JTextPane bodyPane = new JTextPane();
            bodyPane.setContentType("text/plain");   // plain text → 자동 줄바꿈
            bodyPane.setText(post.content());
            bodyPane.setEditable(false);
            bodyPane.setCaretPosition(0);            // 스크롤 맨 위
            JScrollPane bodyScroll = new JScrollPane(bodyPane);
            bodyScroll.setPreferredSize(new Dimension(400, 200));

            /* ─── (C) 댓글 테이블 ─── */
            CommentTableModel cm = new CommentTableModel(comments);
            JTable commentTable  = new JTable(cm);
            commentTable.setRowHeight(22);
            commentTable.getColumnModel().getColumn(0).setMaxWidth(50);   // 번호 폭
            commentTable.getColumnModel().getColumn(2)                    // “내용” 열에만 적용
                         .setCellRenderer(new MultilineCellRenderer());
            commentTable.getColumnModel().getColumn(2).setPreferredWidth(250);
            
            commentTable.addMouseListener(new MouseAdapter() {
                private void maybeShowPopup(MouseEvent e) {
                    if (!e.isPopupTrigger()) return;
                    int row = commentTable.rowAtPoint(e.getPoint());
                    if (row < 0) return;
                    commentTable.setRowSelectionInterval(row, row);
                    CommentDto c = comments.get(row);

                    if (!isAdmin && (c.authorId() == null || !c.authorId().equals(userId))) return;

                    JPopupMenu pop = new JPopupMenu();
                    JMenuItem edit = new JMenuItem("수정");
                    JMenuItem del  = new JMenuItem("삭제");
                    pop.add(edit); pop.add(del);

                    edit.addActionListener(ev -> { dlg.dispose(); editComment(post, c); });
                    del .addActionListener(ev -> { dlg.dispose(); deleteComment(post, c);    }); 


                    pop.show(e.getComponent(), e.getX(), e.getY());
                }
                @Override public void mousePressed (MouseEvent e){ maybeShowPopup(e); }
                @Override public void mouseReleased(MouseEvent e){ maybeShowPopup(e); }
            });

            JScrollPane commentScroll = new JScrollPane(commentTable);
            commentScroll.setPreferredSize(new Dimension(400, 180));

            /* 본문 + 댓글을 수직으로 나누는 SplitPane */
            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              bodyScroll, commentScroll);
            split.setResizeWeight(0.5);   // 초기 비율
            dlg.add(split, BorderLayout.CENTER);
            /* ─── (D) 수정·삭제 버튼 (글쓴이만 보이게) ─── */
            boolean mine = (isAdmin ||p.authorId() == userId);   // PostDto 에 authorId() 가 있다고 가정
            if (mine) {
                JPanel editBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                JButton editBtn = new JButton("수정");
                JButton delBtn  = new JButton("삭제");
                editBar.add(editBtn); editBar.add(delBtn);

                editBtn.addActionListener(ev -> { dlg.dispose(); edit(p);     });
                delBtn .addActionListener(ev -> { dlg.dispose(); remove(p);   });

                header.add(editBar, BorderLayout.EAST);   // header 는 이미 존재하는 패널
            }
            /* ─── (D) 댓글 작성 영역 ─── */
            
            JTextField input = new JTextField();
            JButton addBtn   = new JButton("댓글 작성");
            addBtn.addActionListener(ev -> {
                try {
                    ApiClient.post("/api/posts/" + p.id() + "/comments?userId=" + userId,
                            Map.of("content", input.getText()));
                    JOptionPane.showMessageDialog(dlg, "댓글 작성 완료!");
                    dlg.dispose();
                    reload();                          // 글 목록 새로고침 → 댓글 수 반영
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "댓글 실패: " + ex.getMessage());
                }
            });

            JPanel south = new JPanel(new BorderLayout(5, 5));
            south.add(input, BorderLayout.CENTER);
            south.add(addBtn, BorderLayout.EAST);
            dlg.add(south, BorderLayout.SOUTH);

            dlg.setSize(500, 600);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "상세 조회 실패: " + ex.getMessage());
        }
    }

    /* ───────────────── 댓글용 TableModel ───────────────── */
    private static final class CommentTableModel extends AbstractTableModel {
        private final String[] cols = {"#", "작성자", "내용", "작성일"};
        private final List<CommentDto> list;

        CommentTableModel(List<CommentDto> list) { this.list = list; }

        @Override public int    getRowCount()    { return list.size(); }
        @Override public int    getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override
        public Object getValueAt(int r, int c) {
            CommentDto cmt = list.get(r);
            String formattedDate = LocalDateTime
                    .parse(cmt.createdAt(), ISO_IN)   // String → LocalDateTime
                    .format(OUT_FMT);                  // 원하는 형식으로 변환
            
            return switch (c) {
                case 0 -> r + 1;                // 번호
                case 1 -> cmt.author();         // 작성자
                case 2 -> cmt.content();        // 내용
                case 3 -> formattedDate;      // 작성일
                default -> "";
            };
        }
    }

    private final class PostTableModel extends AbstractTableModel {
        private final String[] cols = {"번호", "제목", "댓글", "작성자", "작성일"};   // ← “댓글” 추가
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override public int    getRowCount()    { return posts.size(); }
        @Override public int    getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override
        public Object getValueAt(int row, int col) {
            PostDto p = posts.get(row);
            return switch (col) {
                case 0 -> posts.size() - row;                 // 번호
                case 1 -> p.title();                          // 제목
                case 2 -> p.commentCount();                   // 댓글 수 (*PostDto에 존재한다고 가정*)
                case 3 -> p.author();                         // 작성자
                case 4 -> p.createdAt().substring(0, 10);     // 작성일 yyyy-MM-dd
                default -> "";
            };
        }
    }
    
    /* 댓글 수정 */
    private void editComment(PostDto post, CommentDto c) {
        JTextArea body = new JTextArea(c.content(), 5, 30);
        int ok = JOptionPane.showConfirmDialog(this,
                new Object[]{"내용", new JScrollPane(body)},
                "댓글 수정", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
        	/* 댓글 수정 */
        	ApiClient.put(
        	    "/api/posts/" + post.id() + "/comments/" + c.id()   // ← postId 포함
        	    + "?userId=" + userId,
        	    Map.of("content", body.getText())
        	);

            JOptionPane.showMessageDialog(this, "수정 완료!");
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "수정 실패: " + ex.getMessage());
        }
    }

    /* 댓글 삭제 */
    private void deleteComment(PostDto post, CommentDto c) {   // ← PostDto 추가
        int res = JOptionPane.showConfirmDialog(this,
                "댓글을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;

        try {
            ApiClient.delete(
                "/api/posts/" + post.id() + "/comments/" + c.id()   // post.id() 사용 가능
                + "?userId=" + userId
            );
            JOptionPane.showMessageDialog(this, "삭제 완료!");
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "삭제 실패: " + ex.getMessage());
        }
    }

    
}
