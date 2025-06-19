// view/LoginPanel.java
package swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;

import java.awt.*;
import java.util.Map;

public class LoginPanel extends JPanel {

    private final JTextField     idField = new JTextField(15);
    private final JPasswordField pwField = new JPasswordField(15);
    private final Main           frame;
    
    /** 라벨을 왼쪽(WEST)·필드를 가운데에 두는 수평 행 생성 */
    private JPanel fieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(60, field.getPreferredSize().height)); // 라벨 폭 고정
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        return row;
    }
    
    public LoginPanel(Main frame) {
        this.frame = frame;

        /* 1) 전체 배경 레이아웃 */
        setLayout(new GridBagLayout());            // 중앙 정렬
        setBackground(new Color(0xF4F6F8));        // 옅은 회색 배경

        /* 2) 가운데 카드 패널 */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 40, 30, 40));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(320, 240));  // 폭 제한

        /* 제목 라벨 */
        JLabel title = new JLabel("LoginPage");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        card.add(title);

        /* 아이디 필드 */
        
        /* 아이디 행 */
        card.add(fieldRow("아이디", idField));
        card.add(space(15));

        /* 비밀번호 행 */
        card.add(fieldRow("비밀번호", pwField));
        card.add(space(25));


        /* 버튼 행 */
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton loginBtn  = new JButton("로그인");
        JButton signupBtn = new JButton("회원가입");
        btnRow.add(loginBtn);
        btnRow.add(signupBtn);
        btnRow.setOpaque(false);
        card.add(btnRow);

        /* 카드 패널을 중앙에 추가 */
        add(card);

        /* 버튼 이벤트 */
        loginBtn .addActionListener(e -> login());
        signupBtn.addActionListener(e -> signup());
    }

    /* ─── 로그인 / 회원가입 메서드(기존과 동일) ─── */
    private void login() {
        try {
            /* ① 서버에 로그인 요청 → JSON 응답 수신 */
            record LoginResponse(long id, String username, boolean admin) {}   // ← 내부 DTO
            Gson gson = new Gson();

            LoginResponse res = gson.fromJson(
                    ApiClient.post("/api/users/login",
                            Map.of("username", idField.getText(),
                                   "password", new String(pwField.getPassword()))),
                    LoginResponse.class
            );

            /* ② 성공 메시지 & 게시판 화면으로 전환 (관리자 여부 포함) */
            JOptionPane.showMessageDialog(this, "로그인 성공!");
            frame.showBoard(res.id(), res.admin());   // ← Main.showBoard 수정 필요

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "로그인 실패: " + ex.getMessage());
        }
    }
    private void signup() {
        try {
            ApiClient.post("/api/users/signup",
                    Map.of("username", idField.getText(),
                           "password", new String(pwField.getPassword())));
            JOptionPane.showMessageDialog(this, "회원가입 완료! 이제 로그인하세요.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "회원가입 실패하였습니다. 다른 아이디로 가입해주세요.");
        }
    }

    /* ─── 수평 간격용 빈 컴포넌트 ─── */
    private Component space(int h) {
        return Box.createVerticalStrut(h);
    }
}
