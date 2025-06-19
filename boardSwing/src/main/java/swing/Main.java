package swing;

import javax.swing.*;

public class Main extends JFrame {

    public Main() {
        super("Swing 게시판");
        setSize(600, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        showLogin();              // 최초 화면 = 로그인
    }

    /* 로그인 패널로 전환 */
    public void showLogin() {
        setContentPane(new LoginPanel(this));
        revalidate(); repaint();
    }

    /* 게시판(로그인 성공 후) 패널로 전환 */
    public void showBoard(long userId, boolean isAdmin) {
        setContentPane(new BoardPanel(this, userId, isAdmin));
        revalidate();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
