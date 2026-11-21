package com.hotelms.ui;

import com.hotelms.config.AppConfig;
import com.hotelms.service.AuthService;
import com.hotelms.ui.components.ThemeManager;
import com.hotelms.ui.components.ToastNotification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {

    private final AuthService authService;

    // Form fields
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;
    private JCheckBox showPasswordBox;

    // Animation
    private float panelOpacity = 0f;
    private Timer fadeTimer;

    public LoginFrame(AuthService authService) {
        this.authService = authService;
        initFrame();
        buildUI();
        startFadeIn();
    }

    // Frame Setup
    private void initFrame() {
        setTitle(AppConfig.APP_NAME + " — Login");
        setSize(AppConfig.WINDOW_WIDTH, AppConfig.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(AppConfig.MIN_WIDTH, AppConfig.MIN_HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(false);
        getContentPane().setBackground(ThemeManager.BG_PRIMARY);
    }

    // UI Construction
    private void buildUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(ThemeManager.BG_PRIMARY);

        root.add(buildLeftPanel());
        root.add(buildRightPanel());

        setContentPane(root);
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Rich dark gradient background
                GradientPaint grad = new GradientPaint(
                    0, 0, new Color(0x0B0E14),
                    getWidth(), getHeight(), new Color(0x151B2B));
                g2.setPaint(grad);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative glow circles
                g2.setColor(new Color(ThemeManager.ACCENT_PRIMARY.getRed(),
                    ThemeManager.ACCENT_PRIMARY.getGreen(),
                    ThemeManager.ACCENT_PRIMARY.getBlue(), 20));
                g2.fillOval(-100, -100, 350, 350);

                g2.setColor(new Color(ThemeManager.ACCENT_INFO.getRed(),
                    ThemeManager.ACCENT_INFO.getGreen(),
                    ThemeManager.ACCENT_INFO.getBlue(), 14));
                g2.fillOval(getWidth() - 220, getHeight() - 220, 400, 400);

                // Subtle accent glow in center
                g2.setColor(new Color(ThemeManager.ACCENT_PRIMARY.getRed(),
                    ThemeManager.ACCENT_PRIMARY.getGreen(),
                    ThemeManager.ACCENT_PRIMARY.getBlue(), 8));
                g2.fillOval(getWidth() / 4, getHeight() / 3, 250, 250);

                // Grid dots pattern
                g2.setColor(new Color(255, 255, 255, 10));
                for (int x = 20; x < getWidth(); x += 28) {
                    for (int y = 20; y < getHeight(); y += 28) {
                        g2.fillOval(x, y, 2, 2);
                    }
                }
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 55, 0, 45));

        // Logo icon box
        JPanel iconBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background for icon
                GradientPaint gp = new GradientPaint(0, 0, ThemeManager.ACCENT_PRIMARY,
                    48, 48, ThemeManager.ACCENT_PRIMARY.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, 48, 48, 14, 14);
                // Icon text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "🏨";
                g2.drawString(icon, (48 - fm.stringWidth(icon)) / 2, 34);
                g2.dispose();
            }

            @Override
            public boolean isOpaque() { return false; }

            @Override
            public Dimension getPreferredSize() { return new Dimension(48, 48); }

            @Override
            public Dimension getMaximumSize() { return new Dimension(48, 48); }
        };
        iconBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Hotel name
        JLabel hotelName = new JLabel(AppConfig.HOTEL_NAME);
        hotelName.setFont(new Font("Segoe UI", Font.BOLD, 30));
        hotelName.setForeground(Color.WHITE);
        hotelName.setAlignmentX(Component.LEFT_ALIGNMENT);

        // App subtitle
        JLabel appSubtitle = new JLabel(AppConfig.APP_SUBTITLE);
        appSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        appSubtitle.setForeground(new Color(ThemeManager.ACCENT_PRIMARY.getRed(),
            ThemeManager.ACCENT_PRIMARY.getGreen(),
            ThemeManager.ACCENT_PRIMARY.getBlue(), 210));
        appSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Divider
        JPanel divider = new JPanel();
        divider.setBackground(new Color(255, 255, 255, 20));
        divider.setPreferredSize(new Dimension(280, 1));
        divider.setMaximumSize(new Dimension(280, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Feature bullets
        String[][] features = {
            {"⚡", "Real-time room availability tracking"},
            {"📄", "Automated billing & invoicing"},
            {"🔐", "Multi-role access control"},
            {"📊", "Analytics & revenue dashboard"},
            {"🖨", "PDF invoice export"}
        };

        content.add(iconBox);
        content.add(Box.createVerticalStrut(20));
        content.add(hotelName);
        content.add(Box.createVerticalStrut(6));
        content.add(appSubtitle);
        content.add(Box.createVerticalStrut(30));
        content.add(divider);
        content.add(Box.createVerticalStrut(24));

        for (String[] feat : features) {
            JPanel featRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            featRow.setOpaque(false);
            featRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            featRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JLabel emojiLbl = new JLabel(feat[0]);
            emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            JLabel textLbl = new JLabel(feat[1]);
            textLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            textLbl.setForeground(new Color(0xA8B2BF));

            featRow.add(emojiLbl);
            featRow.add(textLbl);
            content.add(featRow);
            content.add(Box.createVerticalStrut(8));
        }

        // Version tag
        content.add(Box.createVerticalStrut(24));
        JLabel version = new JLabel("v" + AppConfig.APP_VERSION + "  •  Enterprise Edition");
        version.setFont(ThemeManager.FONT_SMALL);
        version.setForeground(new Color(255, 255, 255, 40));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(version);

        panel.add(content);
        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(ThemeManager.BG_PRIMARY);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(ThemeManager.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 20, 20));
                g2.dispose();
            }

            @Override
            public boolean isOpaque() { return false; }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(44, 44, 44, 44));
        card.setPreferredSize(new Dimension(440, 560));
        card.setMaximumSize(new Dimension(440, 580));

        // Avatar circle
        JPanel avatarCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Outer ring
                g2.setColor(new Color(ThemeManager.ACCENT_PRIMARY.getRed(),
                    ThemeManager.ACCENT_PRIMARY.getGreen(),
                    ThemeManager.ACCENT_PRIMARY.getBlue(), 30));
                g2.fillOval(0, 0, 56, 56);
                // Inner circle
                GradientPaint gp = new GradientPaint(8, 8, ThemeManager.ACCENT_PRIMARY,
                    48, 48, ThemeManager.ACCENT_PRIMARY.darker());
                g2.setPaint(gp);
                g2.fillOval(6, 6, 44, 44);
                // Icon
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "👤";
                g2.drawString(txt, (56 - fm.stringWidth(txt)) / 2, 36);
                g2.dispose();
            }

            @Override
            public boolean isOpaque() { return false; }

            @Override
            public Dimension getPreferredSize() { return new Dimension(56, 56); }

            @Override
            public Dimension getMaximumSize() { return new Dimension(56, 56); }
        };
        avatarCircle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Welcome text
        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcome.setForeground(ThemeManager.TEXT_PRIMARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtext = new JLabel("Sign in to continue to HotelOS");
        subtext.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtext.setForeground(ThemeManager.TEXT_SECONDARY);
        subtext.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(avatarCircle);
        card.add(Box.createVerticalStrut(16));
        card.add(welcome);
        card.add(Box.createVerticalStrut(4));
        card.add(subtext);
        card.add(Box.createVerticalStrut(30));

        // Username
        card.add(fieldLabel("USERNAME"));
        card.add(Box.createVerticalStrut(6));
        usernameField = ThemeManager.textField("Enter your username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(usernameField);
        card.add(Box.createVerticalStrut(18));

        // Password
        card.add(fieldLabel("PASSWORD"));
        card.add(Box.createVerticalStrut(6));
        passwordField = ThemeManager.passwordField("Enter your password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(10));

        // Show password toggle
        showPasswordBox = new JCheckBox("Show password");
        showPasswordBox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        showPasswordBox.setForeground(ThemeManager.TEXT_MUTED);
        showPasswordBox.setOpaque(false);
        showPasswordBox.setFocusPainted(false);
        showPasswordBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showPasswordBox.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordBox.isSelected() ? (char) 0 : '•');
        });
        card.add(showPasswordBox);
        card.add(Box.createVerticalStrut(16));

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        errorLabel.setForeground(ThemeManager.ACCENT_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(6));

        // Login button
        loginButton = ThemeManager.primaryButton("Sign In");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(24));

        // Divider with "Demo" text
        JPanel dividerRow = new JPanel(new BorderLayout(10, 0));
        dividerRow.setOpaque(false);
        dividerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dividerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));

        JPanel line1 = new JPanel();
        line1.setBackground(ThemeManager.BORDER);
        line1.setPreferredSize(new Dimension(0, 1));
        JPanel line2 = new JPanel();
        line2.setBackground(ThemeManager.BORDER);
        line2.setPreferredSize(new Dimension(0, 1));
        JLabel orLabel = new JLabel("DEMO");
        orLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
        orLabel.setForeground(ThemeManager.TEXT_MUTED);
        orLabel.setHorizontalAlignment(SwingConstants.CENTER);

        dividerRow.add(line1, BorderLayout.CENTER);
        dividerRow.add(orLabel, BorderLayout.CENTER);
        // Simple separator instead
        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeManager.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        // Demo credentials
        JLabel demoTitle = new JLabel("QUICK ACCESS");
        demoTitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        demoTitle.setForeground(ThemeManager.TEXT_MUTED);
        demoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(demoTitle);
        card.add(Box.createVerticalStrut(10));

        JPanel demoRow = new JPanel(new GridLayout(1, 2, 10, 0));
        demoRow.setOpaque(false);
        demoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        demoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        demoRow.add(buildDemoCard("Admin", "admin", "Admin@123", ThemeManager.ACCENT_DANGER));
        demoRow.add(buildDemoCard("Receptionist", "receptionist", "Admin@123", ThemeManager.ACCENT_WARNING));

        card.add(demoRow);

        wrapper.add(card);

        // Wire up actions
        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());

        return wrapper;
    }

    private JPanel buildDemoCard(String role, String username, String password, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.BG_TERTIARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Left accent line
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 3, getHeight(), 3, 3);
                g2.dispose();
            }

            @Override
            public boolean isOpaque() { return false; }
        };
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(new EmptyBorder(8, 14, 8, 10));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel roleLbl = new JLabel(role);
        roleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleLbl.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel useLbl = new JLabel("Use →");
        useLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        useLbl.setForeground(accentColor);
        useLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(roleLbl, BorderLayout.CENTER);
        card.add(useLbl, BorderLayout.EAST);

        // Click handler
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                usernameField.setText(username);
                passwordField.setText(password);
                passwordField.requestFocusInWindow();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                useLbl.setForeground(accentColor.brighter());
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                useLbl.setForeground(accentColor);
                card.repaint();
            }
        });

        return card;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(ThemeManager.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // Login Logic
    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            showError("Please enter your username");
            usernameField.requestFocusInWindow();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocusInWindow();
            return;
        }

        loginButton.setText("Signing in...");
        loginButton.setEnabled(false);
        clearError();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    String error = get();
                    if (error == null) {
                        loginSuccess();
                    } else {
                        showError(error);
                        loginButton.setText("Sign In");
                        loginButton.setEnabled(true);
                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                        shakeComponent(loginButton);
                    }
                } catch (Exception ex) {
                    showError("An unexpected error occurred. Check database connection.");
                    loginButton.setText("Sign In");
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void loginSuccess() {
        if (isTranslucencySupported()) {
            Timer fadeOut = new Timer(15, null);
            float[] op = {1.0f};
            fadeOut.addActionListener(e -> {
                op[0] -= 0.06f;
                if (op[0] <= 0f) {
                    ((Timer) e.getSource()).stop();
                    SwingUtilities.invokeLater(() -> {
                        MainFrame mainFrame = new MainFrame(authService);
                        mainFrame.setVisible(true);
                        LoginFrame.this.dispose();
                    });
                }
                try { setOpacity(Math.max(0f, op[0])); } catch (Exception ignored) {}
            });
            fadeOut.start();
        } else {
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame(authService);
                mainFrame.setVisible(true);
                LoginFrame.this.dispose();
            });
        }
    }

    private void showError(String msg) {
        errorLabel.setText("⚠  " + msg);
    }

    private void clearError() {
        errorLabel.setText(" ");
    }

    private void shakeComponent(JComponent comp) {
        final int[] count = {0};
        final int origX = comp.getLocation().x;
        Timer shakeTimer = new Timer(30, null);
        shakeTimer.addActionListener(e -> {
            int offset = (count[0] % 2 == 0) ? 6 : -6;
            comp.setLocation(origX + offset, comp.getLocation().y);
            count[0]++;
            if (count[0] >= 8) {
                comp.setLocation(origX, comp.getLocation().y);
                ((Timer) e.getSource()).stop();
            }
        });
        shakeTimer.start();
    }

    private void startFadeIn() {
        if (isTranslucencySupported()) {
            try { setOpacity(0f); } catch (Exception ignored) {}
            setVisible(true);
            fadeTimer = new Timer(20, null);
            fadeTimer.addActionListener(e -> {
                panelOpacity = Math.min(1f, panelOpacity + 0.05f);
                try { setOpacity(panelOpacity); } catch (Exception ignored) {}
                if (panelOpacity >= 1f) {
                    fadeTimer.stop();
                    usernameField.requestFocusInWindow();
                }
            });
            fadeTimer.start();
        } else {
            setVisible(true);
            usernameField.requestFocusInWindow();
        }
    }

    /** Check if the current graphics device supports window translucency */
    private boolean isTranslucencySupported() {
        try {
            return getGraphicsConfiguration()
                .getDevice()
                .isWindowTranslucencySupported(
                    java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT);
        } catch (Exception e) {
            return false;
        }
    }
}
