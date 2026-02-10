/*
 * Enhanced Purple Board - LMS Terminal
 * Comprehensive learning management system with role-based access
 * for students, instructors, and administrators.
 *
 * Features:
 * - Role-based authentication (Admin, Instructor, Student)
 * - Course and class management
 * - Quiz and assignment creation and submission
 * - Announcement management
 * - Many-to-many relationship management
 * - Enhanced UI with visual improvements
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;

public class terminal extends javax.swing.JPanel {

    /* ── database connection settings ───────────────────────── */
    private static final String DB_URL  =
            "jdbc:mysql://database-1.czka8q6kuuzr.eu-north-1.rds.amazonaws.com:3306/project2";
    private static final String DB_USER = "admin";
    private static final String DB_PASS = "EkGfXpWv2Wtrfb6lHWbi";

    /* ── UI styling constants ─────────────────────────────── */
    private static final Color PURPLE_PRIMARY = new Color(102, 0, 102);
    private static final Color PURPLE_DARK = new Color(80, 0, 80);
    private static final Color TEAL_ACCENT = new Color(0, 153, 153);
    private static final Color DARK_BG = new Color(51, 51, 51);
    private static final Color PANEL_BG = new Color(63, 72, 79);
    private static final Color TEXT_LIGHT = new Color(230, 230, 230);
    private static final Font HEADER_FONT = new Font("Segoe UI Black", Font.BOLD, 22);
    private static final Font SUBHEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    /* ── UI components ──────────────────────────── */
    private JButton AboutUs_btn, Admin_Button, Main_Page_btn;
    private JLabel Login_label, Password_label, Purple_name, Username_label;
    private JTextField password_jTextField1, Username_jTextField1;
    private JPanel Top_panel, choosing_panel;
    private JButton Student_Button, Instructor_Button;
    private JPanel instructorDashboardPanel;
    private JList<String> instructorClassList;
    private DefaultListModel<String> instructorClassesModel = new DefaultListModel<>();
    private DefaultListModel<String> instructorCoursesModel = new DefaultListModel<>();
    private JList<String> instructorCoursesList;

    /* ── admin‑only buttons ─────────────────────────────── */
    private JButton AddStudent_Button, AddInstructor_Button,
                    CreateCourse_Button, CreateClass_Button, SystemSearch_Button,
                    LinkCourseClass_Button, LinkInstructorClass_Button, 
                    AssignInstructorCourseClass_Button;

    /* ── student‑dashboard widgets ──────────── */
    private JPanel dashboard_panel, announcementsCard, coursesCard;
    private JTextArea annArea;
    private DefaultListModel<String> coursesModel = new DefaultListModel<>();
    private JList<String> courseList;
    private JPanel assignmentsPanel, quizzesPanel;
    private DefaultListModel<String> assignmentsModel = new DefaultListModel<>();
    private DefaultListModel<String> quizzesModel = new DefaultListModel<>();
    private JList<String> assignmentsList, quizzesList;
    private Map<Integer, String> quizIdToName = new HashMap<>();
    private Map<Integer, String> assignmentIdToName = new HashMap<>();
    private Set<String> completedActivities = new HashSet<>(); // To track completed quizzes/assignments
    private JPanel gradesPanel;
    private DefaultTableModel gradesTableModel;
    private JTable gradesTable;
    private JTabbedPane studentTabs; // Reference to the existing tabbed pane

    /* ── instructor dashboard widgets ────────── */
    private JButton editAnnouncementBtn, createQuizBtn, createAssignmentBtn, viewQuizzesBtn, viewAssignmentsBtn;
    private JPanel quizAssignmentPanel;
    private int currentSelectedClassId = -1;
    private int currentSelectedCourseId = -1;
    private int currentUserId = -1;

    /* ── profile picture components ─────────── */
    private JLabel profilePicLabel;
    private BufferedImage profileImage;
    private byte[] profileImageBytes;
    private final int PROFILE_PIC_SIZE = 60;

    /* ── runtime state ───────────────────────────────────── */
    private String currentRole = "";   // "admin" | "instructor" | "student"

    /** Creates new form terminal */
    public terminal() { initComponents(); }

    /* =========================================================
                   INITIALISE ALL COMPONENTS
       ========================================================= */
    private void initComponents() {

        /* ========== allocate widgets ========== */
        Top_panel = new JPanel();
        Purple_name = new JLabel();
        Main_Page_btn = new JButton();
        AboutUs_btn = new JButton();
        choosing_panel = new JPanel();
        Instructor_Button = new JButton();
        Admin_Button = new JButton();
        Student_Button = new JButton();
        AddStudent_Button = new JButton();
        AddInstructor_Button = new JButton();
        CreateCourse_Button = new JButton();
        CreateClass_Button = new JButton();
        SystemSearch_Button = new JButton();
        LinkCourseClass_Button = new JButton();
        LinkInstructorClass_Button = new JButton();
        AssignInstructorCourseClass_Button = new JButton();
        password_jTextField1 = new JTextField();
        Username_jTextField1 = new JTextField();
        Password_label = new JLabel();
        Username_label = new JLabel();
        Login_label = new JLabel();
        
        // Dashboard components
        dashboard_panel = new JPanel();
        announcementsCard = new JPanel();
        coursesCard = new JPanel();
        annArea = new JTextArea();
        courseList = new JList<>();
        
        // Grades Panel
gradesPanel = new JPanel(new BorderLayout(5, 5));
gradesPanel.setBackground(PANEL_BG);
gradesPanel.setBorder(createRoundedBorder(10, TEAL_ACCENT));

JLabel gradesTitle = new JLabel("My Grades", JLabel.CENTER);
gradesTitle.setFont(HEADER_FONT);
gradesTitle.setForeground(TEXT_LIGHT);
gradesTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

// Create table model with columns
String[] gradeColumns = {"Course", "Assignment/Quiz", "Score", "Max Points", "Percentage", "Submission Date"};
gradesTableModel = new DefaultTableModel(gradeColumns, 0);
gradesTable = new JTable(gradesTableModel);

// Style the table
gradesTable.setFont(REGULAR_FONT);
gradesTable.setBackground(new Color(80, 90, 100));
gradesTable.setForeground(TEXT_LIGHT);
gradesTable.setGridColor(new Color(100, 100, 100));
gradesTable.setRowHeight(25);
gradesTable.getTableHeader().setFont(SUBHEADER_FONT);
gradesTable.getTableHeader().setBackground(PURPLE_DARK);
gradesTable.getTableHeader().setForeground(TEXT_LIGHT);

// Set custom renderer for percentage column to show color-coded percentages
gradesTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (value != null) {
            try {
                String strValue = value.toString().replace("%", "").trim();
                if (!strValue.isEmpty()) {
                    double percentage = Double.parseDouble(strValue);
                    if (percentage >= 90) {
                        c.setForeground(new Color(0, 200, 0)); // Green for A
                    } else if (percentage >= 80) {
                        c.setForeground(new Color(144, 238, 144)); // Light green for B
                    } else if (percentage >= 70) {
                        c.setForeground(new Color(255, 255, 0)); // Yellow for C
                    } else if (percentage >= 60) {
                        c.setForeground(new Color(255, 165, 0)); // Orange for D
                    } else {
                        c.setForeground(new Color(255, 0, 0)); // Red for F
                    }
                } else {
                    c.setForeground(TEXT_LIGHT); // Default color for empty string
                }
            } catch (NumberFormatException e) {
                // If we can't parse a number, just use the default text color
                c.setForeground(TEXT_LIGHT);
            }
        }
        
        return c;
    }
});

// Create scroll pane for the table
JScrollPane gradesScroll = new JScrollPane(gradesTable);
gradesScroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

// Add components to the grades panel
gradesPanel.add(gradesTitle, BorderLayout.NORTH);
gradesPanel.add(gradesScroll, BorderLayout.CENTER);


        // Student interface components
        assignmentsPanel = new JPanel();
        quizzesPanel = new JPanel();
        assignmentsModel = new DefaultListModel<>();
        quizzesModel = new DefaultListModel<>();
        assignmentsList = new JList<>(assignmentsModel);
        quizzesList = new JList<>(quizzesModel);
        
        // Instructor components
        instructorDashboardPanel = new JPanel();
        instructorClassesModel = new DefaultListModel<>();
        instructorCoursesModel = new DefaultListModel<>();
        instructorClassList = new JList<>(instructorClassesModel);
        instructorCoursesList = new JList<>(instructorCoursesModel);
        editAnnouncementBtn = createStyledButton("Edit Announcement");
        createQuizBtn = createStyledButton("Create Quiz");
        createAssignmentBtn = createStyledButton("Create Assignment");
        viewQuizzesBtn = createStyledButton("View Quizzes");
        viewAssignmentsBtn = createStyledButton("View Assignments");
        quizAssignmentPanel = new JPanel();
    
        /* ---------- instructor dashboard panel ---------- */
        instructorDashboardPanel.setBackground(DARK_BG);
        instructorDashboardPanel.setLayout(new BorderLayout(10, 10));
        
        // Create a tabbed pane for instructor views
        JTabbedPane instructorTabs = new JTabbedPane();
        instructorTabs.setFont(SUBHEADER_FONT);
        instructorTabs.setForeground(TEXT_LIGHT);
        instructorTabs.setBackground(DARK_BG);
        
        // Classes Tab
        JPanel classesTab = new JPanel(new BorderLayout(5, 5));
        classesTab.setBackground(DARK_BG);
        
        JPanel classHeader = new JPanel(new BorderLayout());
        classHeader.setBackground(PANEL_BG);
        JLabel classTitle = new JLabel("Your Classes", JLabel.CENTER);
        classTitle.setFont(HEADER_FONT);
        classTitle.setForeground(TEXT_LIGHT);
        classHeader.add(classTitle, BorderLayout.CENTER);
        
        instructorClassList.setFont(REGULAR_FONT);
        instructorClassList.setBackground(new Color(80, 90, 100));
        instructorClassList.setForeground(TEXT_LIGHT);
        instructorClassList.setSelectionBackground(TEAL_ACCENT);
        instructorClassList.setSelectionForeground(Color.WHITE);
        instructorClassList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        instructorClassList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && instructorClassList.getSelectedIndex() != -1) {
                    String selected = instructorClassList.getSelectedValue();
                    if (selected != null && !selected.equals("No classes assigned")) {
                        // Extract class ID from the selection string
                        String idStr = selected.substring(selected.indexOf(":")+1, selected.indexOf("-")).trim();
                        try {
                            currentSelectedClassId = Integer.parseInt(idStr);
                        } catch (NumberFormatException ex) {
                            currentSelectedClassId = -1;
                        }
                    }
                }
            }
        });
        
        JScrollPane classScroll = new JScrollPane(instructorClassList);
        classScroll.setBorder(BorderFactory.createEmptyBorder());
        
        // Instructor control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(PANEL_BG);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        editAnnouncementBtn.addActionListener(this::editAnnouncementActionPerformed);
        createQuizBtn.addActionListener(this::createQuizActionPerformed);
        createAssignmentBtn.addActionListener(this::createAssignmentActionPerformed);
        viewQuizzesBtn.addActionListener(this::viewQuizzesActionPerformed);
        viewAssignmentsBtn.addActionListener(this::viewAssignmentsActionPerformed);
        
        controlPanel.add(editAnnouncementBtn);
        controlPanel.add(createQuizBtn);
        controlPanel.add(createAssignmentBtn);
        controlPanel.add(viewQuizzesBtn);
        controlPanel.add(viewAssignmentsBtn);
        
        classesTab.add(classHeader, BorderLayout.NORTH);
        classesTab.add(classScroll, BorderLayout.CENTER);
        classesTab.add(controlPanel, BorderLayout.SOUTH);
        
        // Courses Tab
        JPanel instructorCoursesTab = new JPanel(new BorderLayout(5, 5));
        instructorCoursesTab.setBackground(DARK_BG);
        
        JPanel coursesHeader = new JPanel(new BorderLayout());
        coursesHeader.setBackground(PANEL_BG);
        JLabel coursesTitle = new JLabel("Your Courses & Classes", JLabel.CENTER);
        coursesTitle.setFont(HEADER_FONT);
        coursesTitle.setForeground(TEXT_LIGHT);
        coursesHeader.add(coursesTitle, BorderLayout.CENTER);
        
        instructorCoursesList = new JList<>(instructorCoursesModel);
        instructorCoursesList.setFont(REGULAR_FONT);
        instructorCoursesList.setBackground(new Color(80, 90, 100));
        instructorCoursesList.setForeground(TEXT_LIGHT);
        instructorCoursesList.setSelectionBackground(TEAL_ACCENT);
        instructorCoursesList.setSelectionForeground(Color.WHITE);
        instructorCoursesList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane coursesScroll = new JScrollPane(instructorCoursesList);
        coursesScroll.setBorder(BorderFactory.createEmptyBorder());
        
        instructorCoursesTab.add(coursesHeader, BorderLayout.NORTH);
        instructorCoursesTab.add(coursesScroll, BorderLayout.CENTER);
        
        // Add tabs to the tabbed pane
        instructorTabs.addTab("Classes", classesTab);
        instructorTabs.addTab("Courses", instructorCoursesTab);
        
        // Add padding around the tabs
        JPanel tabsContainer = new JPanel(new BorderLayout());
        tabsContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        tabsContainer.setBackground(DARK_BG);
        tabsContainer.add(instructorTabs, BorderLayout.CENTER);
        
        instructorDashboardPanel.add(tabsContainer, BorderLayout.CENTER);
        instructorDashboardPanel.setVisible(false);
    
        /* ---------- top panel ---------- */
        setBackground(DARK_BG);
        Top_panel.setBackground(PURPLE_PRIMARY);
        Top_panel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, TEAL_ACCENT));
    
        Purple_name.setFont(new Font("Segoe UI Historic", Font.BOLD, 18));
        Purple_name.setForeground(TEAL_ACCENT);
        Purple_name.setHorizontalAlignment(SwingConstants.LEFT);
        try {
            java.net.URL url = getClass().getResource("/web-design (1).png");
            if (url != null) {
                Purple_name.setIcon(new ImageIcon(url));
            }
        } catch (Exception e) {
            // Continue without icon
        }
        Purple_name.setText("Purple Board");
    
        Main_Page_btn.setBackground(PURPLE_PRIMARY);
        Main_Page_btn.setFont(new Font("Segoe UI Historic", Font.BOLD, 18));
        Main_Page_btn.setForeground(TEAL_ACCENT);
        Main_Page_btn.setText("Main Page");
        Main_Page_btn.setBorder(null);
        Main_Page_btn.setFocusPainted(false);
        Main_Page_btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Main_Page_btn.addActionListener(this::Main_Page_btnActionPerformed);
    
        AboutUs_btn.setBackground(PURPLE_PRIMARY);
        AboutUs_btn.setFont(new Font("Segoe UI Historic", Font.BOLD, 18));
        AboutUs_btn.setForeground(TEAL_ACCENT);
        AboutUs_btn.setText("About Us");
        AboutUs_btn.setBorder(null);
        AboutUs_btn.setFocusPainted(false);
        AboutUs_btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        AboutUs_btn.addActionListener(this::AboutUs_btnActionPerformed);
        
        // Create profile picture label for top panel
        profilePicLabel = new JLabel();
        profilePicLabel.setPreferredSize(new Dimension(PROFILE_PIC_SIZE, PROFILE_PIC_SIZE));
        profilePicLabel.setBorder(BorderFactory.createLineBorder(TEAL_ACCENT, 2));
        profilePicLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profilePicLabel.setToolTipText("Click to change profile picture");
        
        // Set default profile icon
        try {
            BufferedImage defaultIcon = new BufferedImage(PROFILE_PIC_SIZE, PROFILE_PIC_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = defaultIcon.createGraphics();
            g2d.setColor(new Color(150, 150, 150));
            g2d.fillOval(0, 0, PROFILE_PIC_SIZE, PROFILE_PIC_SIZE);
            g2d.setColor(new Color(200, 200, 200));
            g2d.fillOval(PROFILE_PIC_SIZE/4, PROFILE_PIC_SIZE/4, PROFILE_PIC_SIZE/2, PROFILE_PIC_SIZE/2);
            g2d.dispose();
            
            profilePicLabel.setIcon(new ImageIcon(defaultIcon));
        } catch (Exception e) {
            System.err.println("Error creating default profile icon: " + e.getMessage());
        }
        
        // Add click listener to profile picture
        profilePicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!currentRole.isEmpty() && currentUserId != -1) {
                    selectProfilePicture();
                } else {
                    showStyledMessageDialog(terminal.this, 
                        "Please log in first to change your profile picture.", 
                        "Login Required", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    
        GroupLayout Top_panelLayout = new GroupLayout(Top_panel);
        Top_panel.setLayout(Top_panelLayout);
        Top_panelLayout.setHorizontalGroup(
            Top_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Purple_name, 177, 177, 177)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Main_Page_btn)
                .addGap(29)
                .addComponent(AboutUs_btn)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(profilePicLabel)
                .addContainerGap()
        );
        Top_panelLayout.setVerticalGroup(
            Top_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(Top_panelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(Purple_name, 76, 76, 76)
                    .addComponent(Main_Page_btn)
                    .addComponent(AboutUs_btn)
                    .addComponent(profilePicLabel, PROFILE_PIC_SIZE, PROFILE_PIC_SIZE, PROFILE_PIC_SIZE))
                .addContainerGap()
        );
    
        /* ---------- choosing panel ---------- */
        choosing_panel.setBackground(DARK_BG);
    
        Student_Button = makeRoleBtn("Student", "/reading-book.png", TEAL_ACCENT, SUBHEADER_FONT, this::Student_ButtonActionPerformed);
        Instructor_Button = makeRoleBtn("Instructor","/instructor.png", TEAL_ACCENT, SUBHEADER_FONT, this::Instructor_ButtonActionPerformed);
        Admin_Button = makeRoleBtn("Admin", "/admin.png", TEAL_ACCENT, SUBHEADER_FONT, this::Admin_ButtonActionPerformed);
    
        AddStudent_Button = makeAdminBtn("Add Student", "/reading-book.png", TEAL_ACCENT, REGULAR_FONT);
        AddInstructor_Button = makeAdminBtn("Add Instructor", "/instructor.png", TEAL_ACCENT, REGULAR_FONT);
        CreateCourse_Button = makeAdminBtn("Create Course", "/create_course .png", TEAL_ACCENT, REGULAR_FONT);
        CreateClass_Button = makeAdminBtn("Create Class", "/create_class.png", TEAL_ACCENT, REGULAR_FONT);
        SystemSearch_Button = makeAdminBtn("System Search", "/search.png", TEAL_ACCENT, REGULAR_FONT);
        LinkCourseClass_Button = makeAdminBtn("Link Course-Class", "/link.png", TEAL_ACCENT, REGULAR_FONT);
        LinkInstructorClass_Button = makeAdminBtn("Link Instructor-Class", "/link.png", TEAL_ACCENT, REGULAR_FONT);
        AssignInstructorCourseClass_Button = makeAdminBtn("Assign Instructor", "/assign.png", TEAL_ACCENT, REGULAR_FONT);
    
        // Add listeners
        AddStudent_Button.addActionListener(this::AddStudent_ButtonActionPerformed);
        AddInstructor_Button.addActionListener(this::AddInstructor_ButtonActionPerformed);
        CreateClass_Button.addActionListener(this::CreateClass_ButtonActionPerformed);
        CreateCourse_Button.addActionListener(this::CreateCourse_ButtonActionPerformed);
        SystemSearch_Button.addActionListener(this::SystemSearch_ButtonActionPerformed);
        LinkCourseClass_Button.addActionListener(this::LinkCourseClassActionPerformed);
        LinkInstructorClass_Button.addActionListener(this::LinkInstructorClassActionPerformed);
        AssignInstructorCourseClass_Button.addActionListener(this::AssignInstructorCourseClassActionPerformed);
    
        setAdminButtonsVisible(false);
    
        // Style the login fields - Made wider as requested
        password_jTextField1 = new JTextField();
        password_jTextField1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, TEAL_ACCENT),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        password_jTextField1.setFont(REGULAR_FONT);
        password_jTextField1.setBackground(new Color(70, 70, 70));
        password_jTextField1.setForeground(TEXT_LIGHT);
        password_jTextField1.setCaretColor(TEXT_LIGHT);
        password_jTextField1.addActionListener(this::password_jTextField1ActionPerformed);
        password_jTextField1.setPreferredSize(new Dimension(300, 30));
        password_jTextField1.setMinimumSize(new Dimension(300, 30));
        Username_jTextField1 = new JTextField();
        Username_jTextField1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, TEAL_ACCENT),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        Username_jTextField1.setFont(REGULAR_FONT);
        Username_jTextField1.setBackground(new Color(70, 70, 70));
        Username_jTextField1.setForeground(TEXT_LIGHT);
        Username_jTextField1.setCaretColor(TEXT_LIGHT);
        Username_jTextField1.setPreferredSize(new Dimension(300, 30));
        Username_jTextField1.setMinimumSize(new Dimension(300, 30));
        Username_jTextField1.addActionListener(this::password_jTextField1ActionPerformed);
    
        Password_label.setFont(SUBHEADER_FONT);
        Password_label.setForeground(TEAL_ACCENT);
        Password_label.setText("Password:");
    
        Username_label.setFont(SUBHEADER_FONT);
        Username_label.setForeground(TEAL_ACCENT);
        Username_label.setText("Username:");
    
        Login_label.setFont(HEADER_FONT);
        Login_label.setForeground(TEAL_ACCENT);
        Login_label.setText("Login");
    
        // Create a panel for the login form with better styling
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(DARK_BG);
        loginPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 15, 5);
        
        loginPanel.add(Login_label, gbc);
        
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JPanel usernamePanel = new JPanel(new BorderLayout(10, 0));
        usernamePanel.setBackground(DARK_BG);
        usernamePanel.add(Username_label, BorderLayout.WEST);
        usernamePanel.add(Username_jTextField1, BorderLayout.CENTER);
        loginPanel.add(usernamePanel, gbc);
        
        JPanel passwordPanel = new JPanel(new BorderLayout(10, 0));
        passwordPanel.setBackground(DARK_BG);
        passwordPanel.add(Password_label, BorderLayout.WEST);
        passwordPanel.add(password_jTextField1, BorderLayout.CENTER);
        loginPanel.add(passwordPanel, gbc);
        
        // Removed login button, press Enter in password field to login
        
        // Main role selection panel
        JPanel rolePanel = new JPanel(new GridBagLayout());
        rolePanel.setBackground(DARK_BG);
        rolePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        
        GridBagConstraints roleGbc = new GridBagConstraints();
        roleGbc.gridwidth = GridBagConstraints.REMAINDER;
        roleGbc.fill = GridBagConstraints.HORIZONTAL;
        roleGbc.insets = new Insets(8, 0, 8, 0);
        
        // Removed "Select Your Role" label
        
        rolePanel.add(Student_Button, roleGbc);
        rolePanel.add(Instructor_Button, roleGbc);
        rolePanel.add(Admin_Button, roleGbc);
        
        // Admin buttons panel
        JPanel adminPanel = new JPanel(new GridBagLayout());
        adminPanel.setBackground(DARK_BG);
        adminPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        GridBagConstraints adminGbc = new GridBagConstraints();
        adminGbc.gridwidth = GridBagConstraints.REMAINDER;
        adminGbc.fill = GridBagConstraints.HORIZONTAL;
        adminGbc.insets = new Insets(5, 0, 5, 0);
        
        // Removed "Admin Functions" label
        
        adminPanel.add(AddStudent_Button, adminGbc);
        adminPanel.add(AddInstructor_Button, adminGbc);
        adminPanel.add(CreateCourse_Button, adminGbc);
        adminPanel.add(CreateClass_Button, adminGbc);
        adminPanel.add(LinkCourseClass_Button, adminGbc);
        adminPanel.add(LinkInstructorClass_Button, adminGbc);
        adminPanel.add(AssignInstructorCourseClass_Button, adminGbc);
        adminPanel.add(SystemSearch_Button, adminGbc);
        
        // Combine all panels
        choosing_panel.setLayout(new BorderLayout());
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(DARK_BG);
        
        GridBagConstraints centerGbc = new GridBagConstraints();
        centerGbc.gridwidth = GridBagConstraints.REMAINDER;
        centerGbc.fill = GridBagConstraints.HORIZONTAL;
        centerGbc.insets = new Insets(10, 0, 10, 0);
        
        centerPanel.add(loginPanel, centerGbc);
        centerPanel.add(rolePanel, centerGbc);
        centerPanel.add(adminPanel, centerGbc);
        
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(DARK_BG);
        scrollPane.getViewport().setBackground(DARK_BG);
        
        choosing_panel.add(scrollPane, BorderLayout.CENTER);
    
        /* ---------- student dashboard ---------- */
        dashboard_panel = new JPanel();
        dashboard_panel.setBackground(DARK_BG);
        dashboard_panel.setLayout(new BorderLayout(10, 10));
        
        // Create a tabbed pane for student views
        JTabbedPane studentTabs = new JTabbedPane();
        studentTabs.setFont(SUBHEADER_FONT);
        studentTabs.setForeground(TEXT_LIGHT);
        studentTabs.setBackground(DARK_BG);
        studentTabs.addTab("My Grades", gradesPanel);

        
        // Announcements Panel
        announcementsCard = new JPanel(new BorderLayout(5, 5));
        announcementsCard.setBackground(PANEL_BG);
        announcementsCard.setBorder(createRoundedBorder(10, TEAL_ACCENT));
        
        JLabel annTitle = new JLabel("Announcements", JLabel.CENTER);
        annTitle.setFont(HEADER_FONT);
        annTitle.setForeground(TEXT_LIGHT);
        annTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        annArea = new JTextArea();
        annArea.setText("No announcements...");
        annArea.setEditable(false);
        annArea.setFont(REGULAR_FONT);
        annArea.setBackground(new Color(80, 90, 100));
        annArea.setForeground(TEXT_LIGHT);
        annArea.setLineWrap(true);
        annArea.setWrapStyleWord(true);
        annArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane annScroll = new JScrollPane(annArea);
        annScroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        announcementsCard.add(annTitle, BorderLayout.NORTH);
        announcementsCard.add(annScroll, BorderLayout.CENTER);
        
        // Courses Panel
        coursesCard = new JPanel(new BorderLayout(5, 5));
        coursesCard.setBackground(PANEL_BG);
        coursesCard.setBorder(createRoundedBorder(10, TEAL_ACCENT));
        
        JLabel courseTitle = new JLabel("My Courses", JLabel.CENTER);
        courseTitle.setFont(HEADER_FONT);
        courseTitle.setForeground(TEXT_LIGHT);
        courseTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        courseList = new JList<>(coursesModel);
        courseList.setFont(REGULAR_FONT);
        courseList.setBackground(new Color(80, 90, 100));
        courseList.setForeground(TEXT_LIGHT);
        courseList.setSelectionBackground(TEAL_ACCENT);
        courseList.setSelectionForeground(Color.WHITE);
        courseList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        courseList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && courseList.getSelectedIndex() != -1) {
                    String courseName = courseList.getSelectedValue();
                    if (courseName != null && !courseName.equals("No courses found")) {
                        loadCourseContent(courseName);
                    }
                }
            }
        });
        
        JScrollPane courseScroll = new JScrollPane(courseList);
        courseScroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        coursesCard.add(courseTitle, BorderLayout.NORTH);
        coursesCard.add(courseScroll, BorderLayout.CENTER);
        
        // Create assignment and quiz panels
        assignmentsPanel = new JPanel(new BorderLayout(5, 5));
        assignmentsPanel.setBackground(PANEL_BG);
        assignmentsPanel.setBorder(createRoundedBorder(10, TEAL_ACCENT));
        
        quizzesPanel = new JPanel(new BorderLayout(5, 5));
        quizzesPanel.setBackground(PANEL_BG);
        quizzesPanel.setBorder(createRoundedBorder(10, TEAL_ACCENT));
        
        JLabel assignmentsTitle = new JLabel("Assignments", JLabel.CENTER);
        assignmentsTitle.setFont(SUBHEADER_FONT);
        assignmentsTitle.setForeground(TEXT_LIGHT);
        assignmentsTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel quizzesTitle = new JLabel("Quizzes", JLabel.CENTER);
        quizzesTitle.setFont(SUBHEADER_FONT);
        quizzesTitle.setForeground(TEXT_LIGHT);
        quizzesTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        assignmentsList = new JList<>(assignmentsModel);
        assignmentsList.setFont(REGULAR_FONT);
        assignmentsList.setBackground(new Color(80, 90, 100));
        assignmentsList.setForeground(TEXT_LIGHT);
        assignmentsList.setSelectionBackground(TEAL_ACCENT);
        assignmentsList.setSelectionForeground(Color.WHITE);
        assignmentsList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        assignmentsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    takeAssignment();
                }
            }
        });
        
        JScrollPane assignmentsScroll = new JScrollPane(assignmentsList);
        assignmentsScroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        JButton takeAssignmentBtn = createStyledButton("Take Assignment");
        takeAssignmentBtn.addActionListener(e -> takeAssignment());
        
        JPanel assignmentButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        assignmentButtonPanel.setBackground(PANEL_BG);
        assignmentButtonPanel.add(takeAssignmentBtn);
        
        assignmentsPanel.add(assignmentsTitle, BorderLayout.NORTH);
        assignmentsPanel.add(assignmentsScroll, BorderLayout.CENTER);
        assignmentsPanel.add(assignmentButtonPanel, BorderLayout.SOUTH);
        
        quizzesList = new JList<>(quizzesModel);
        quizzesList.setFont(REGULAR_FONT);
        quizzesList.setBackground(new Color(80, 90, 100));
        quizzesList.setForeground(TEXT_LIGHT);
        quizzesList.setSelectionBackground(TEAL_ACCENT);
        quizzesList.setSelectionForeground(Color.WHITE);
        quizzesList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        quizzesList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    takeQuiz();
                }
            }
        });
        
        JScrollPane quizzesScroll = new JScrollPane(quizzesList);
        quizzesScroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        JButton takeQuizBtn = createStyledButton("Take Quiz");
        takeQuizBtn.addActionListener(e -> takeQuiz());
        
        JPanel quizButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        quizButtonPanel.setBackground(PANEL_BG);
        quizButtonPanel.add(takeQuizBtn);
        
        quizzesPanel.add(quizzesTitle, BorderLayout.NORTH);
        quizzesPanel.add(quizzesScroll, BorderLayout.CENTER);
        quizzesPanel.add(quizButtonPanel, BorderLayout.SOUTH);
        
        // Courses Tab - Using studentCoursesTab to avoid duplicate variable
        JPanel studentCoursesTab = new JPanel(new GridBagLayout());
        studentCoursesTab.setBackground(DARK_BG);
        
        GridBagConstraints courseGbc = new GridBagConstraints();
        courseGbc.fill = GridBagConstraints.BOTH;
        courseGbc.weightx = 1.0;
        courseGbc.weighty = 0.6;
        courseGbc.gridx = 0;
        courseGbc.gridy = 0;
        courseGbc.gridwidth = 2;
        courseGbc.insets = new Insets(10, 10, 5, 10);
        
        studentCoursesTab.add(coursesCard, courseGbc);
        
        courseGbc.gridwidth = 1;
        courseGbc.weightx = 0.5;
        courseGbc.weighty = 0.4;
        courseGbc.gridy = 1;
        courseGbc.insets = new Insets(5, 10, 10, 5);
        
        studentCoursesTab.add(assignmentsPanel, courseGbc);
        
        courseGbc.gridx = 1;
        courseGbc.insets = new Insets(5, 5, 10, 10);
        
        studentCoursesTab.add(quizzesPanel, courseGbc);
        
        // Announcements Tab
        JPanel announcementsTab = new JPanel(new BorderLayout(10, 10));
        announcementsTab.setBackground(DARK_BG);
        announcementsTab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        announcementsTab.add(announcementsCard, BorderLayout.CENTER);
        
        // Add tabs to the tabbed pane
        studentTabs.addTab("Courses", studentCoursesTab);
        studentTabs.addTab("Announcements", announcementsTab);
        
        // Add padding around the tabs
        JPanel studentTabsContainer = new JPanel(new BorderLayout());
        studentTabsContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        studentTabsContainer.setBackground(DARK_BG);
        studentTabsContainer.add(studentTabs, BorderLayout.CENTER);
        
        dashboard_panel.add(studentTabsContainer, BorderLayout.CENTER);
        dashboard_panel.setVisible(false);
    
        /* ---------- root layout ---------- */
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(Top_panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(choosing_panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(dashboard_panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(instructorDashboardPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(Top_panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(choosing_panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(dashboard_panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(instructorDashboardPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    
        setLoginVisible(false);
    }


    private void loadStudentGrades(int studentId) {
        // Clear existing data
        gradesTableModel.setRowCount(0);
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // First, load quiz grades
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(
                    """
                    SELECT c.courses_Name, q.Quiz_Name, qg.score, q.Quiz_Grade, 
                           (qg.score / q.Quiz_Grade * 100) as percentage, 
                           qg.submission_date
                    FROM student_quiz_grades qg
                    JOIN quizzes q ON qg.quiz_id = q.Quiz_id
                    JOIN classes cl ON q.classes_id = cl.Classes_id
                    JOIN course_class cc ON cl.Classes_id = cc.class_id
                    JOIN courses c ON cc.course_id = c.courses_id
                    WHERE qg.student_id = ?
                    ORDER BY c.courses_Name, qg.submission_date DESC
                    """)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object[] row = new Object[6];
                        row[0] = rs.getString(1); // Course name
                        row[1] = "Quiz: " + rs.getString(2); // Quiz name
                        row[2] = rs.getDouble(3); // Score
                        row[3] = rs.getDouble(4); // Max points
                        row[4] = String.format("%.1f%%", rs.getDouble(5)); // Percentage
                        row[5] = formatDate(rs.getTimestamp(6)); // Submission date
                        
                        gradesTableModel.addRow(row);
                    }
                }
            }
            
            // Then, load assignment grades
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(
                    """
                    SELECT c.courses_Name, a.Assignment_Name, ag.score, a.Assignment_Grade, 
                           (ag.score / a.Assignment_Grade * 100) as percentage, 
                           ag.submission_date
                    FROM student_assignment_grades ag
                    JOIN assignments a ON ag.assignment_id = a.Assignment_id
                    JOIN classes cl ON a.classes_id = cl.Classes_id
                    JOIN course_class cc ON cl.Classes_id = cc.class_id
                    JOIN courses c ON cc.course_id = c.courses_id
                    WHERE ag.student_id = ?
                    ORDER BY c.courses_Name, ag.submission_date DESC
                    """)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object[] row = new Object[6];
                        row[0] = rs.getString(1); // Course name
                        row[1] = "Assignment: " + rs.getString(2); // Assignment name
                        row[2] = rs.getDouble(3); // Score
                        row[3] = rs.getDouble(4); // Max points
                        row[4] = String.format("%.1f%%", rs.getDouble(5)); // Percentage
                        row[5] = formatDate(rs.getTimestamp(6)); // Submission date
                        
                        gradesTableModel.addRow(row);
                    }
                }
            }
            
            // If no grades found, add a message row
            if (gradesTableModel.getRowCount() == 0) {
                Object[] noGradeRow = new Object[6];
                noGradeRow[0] = "No grades available";
                for (int i = 1; i < 6; i++) {
                    noGradeRow[i] = "";
                }
                gradesTableModel.addRow(noGradeRow);
            }
            
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error loading grades:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
            
            // Add error message to table
            Object[] errorRow = new Object[6];
            errorRow[0] = "Error loading grades";
            errorRow[1] = ex.getMessage();
            for (int i = 2; i < 6; i++) {
                errorRow[i] = "";
            }
            gradesTableModel.addRow(errorRow);
        }
    }
    
/**
 * Helper method to format datetime for display
 */
private String formatDate(Timestamp timestamp) {
    if (timestamp == null) return "N/A";
    
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
    return sdf.format(timestamp);
}
    

    /* =========================================================
       Helper method to take a quiz
       ========================================================= */
       private void takeQuiz() {
        if (quizzesList.getSelectedIndex() == -1) {
            showStyledMessageDialog(this, "Please select a quiz first.", "No Quiz Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selectedQuiz = quizzesList.getSelectedValue();
        if (selectedQuiz.startsWith("No quizzes")) {
            return;
        }
        
        // Extract quiz ID from the list item
        int quizId;
        try {
            quizId = Integer.parseInt(selectedQuiz.substring(0, selectedQuiz.indexOf(" -")).trim());
        } catch (Exception e) {
            showStyledMessageDialog(this, "Error parsing quiz ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if quiz has already been taken by this student
        String activityKey = "quiz_" + currentUserId + "_" + quizId;
        if (completedActivities.contains(activityKey)) {
            showStyledMessageDialog(this, "You have already completed this quiz.", 
                    "Quiz Already Taken", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Fetch quiz questions
        ArrayList<Question> questions = new ArrayList<>();
        String quizName = "";
        int score = 0; // Define score here, outside the try block
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // First, get the quiz name and grade
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(
                     "SELECT Quiz_Name, Quiz_Grade FROM quizzes WHERE Quiz_id = ?")) {
                ps.setInt(1, quizId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        quizName = rs.getString(1);
                    } else {
                        showStyledMessageDialog(this, "Quiz not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            
            // Then get the questions
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(
                     """
                     SELECT question_id, question_text, answer_option_1, answer_option_2, 
                            answer_option_3, answer_option_4, correct_answer
                     FROM questions
                     WHERE quiz_id = ?
                     """)) {
                ps.setInt(1, quizId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Question q = new Question();
                        q.id = rs.getInt(1);
                        q.text = rs.getString(2);
                        q.options.add(rs.getString(3));
                        q.options.add(rs.getString(4));
                        
                        String option3 = rs.getString(5);
                        if (option3 != null && !option3.isEmpty()) {
                            q.options.add(option3);
                        }
                        
                        String option4 = rs.getString(6);
                        if (option4 != null && !option4.isEmpty()) {
                            q.options.add(option4);
                        }
                        
                        q.correctAnswer = rs.getInt(7);
                        questions.add(q);
                    }
                }
            }
            
            if (questions.isEmpty()) {
                showStyledMessageDialog(this, "This quiz has no questions.", "Empty Quiz", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Create the quiz taking UI
            score = showQuizDialog(quizName, questions); // assign to the score variable we defined earlier
            
            // Mark quiz as completed
            completedActivities.add(activityKey);
            
            // Show result
            JPanel resultPanel = new JPanel(new BorderLayout(0, 10));
            resultPanel.setBackground(DARK_BG);
            
            JLabel titleLabel = new JLabel("Quiz Results", JLabel.CENTER);
            titleLabel.setFont(HEADER_FONT);
            titleLabel.setForeground(TEAL_ACCENT);
            
            JLabel scoreLabel = new JLabel(String.format(
                "You scored %d out of %d points (%.1f%%)", 
                score, questions.size(), (100.0 * score / questions.size())),
                JLabel.CENTER);
            scoreLabel.setFont(SUBHEADER_FONT);
            scoreLabel.setForeground(TEXT_LIGHT);
            
            resultPanel.add(titleLabel, BorderLayout.NORTH);
            resultPanel.add(scoreLabel, BorderLayout.CENTER);
            
            showStyledOptionDialog(this, resultPanel, "Quiz Completed", 
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, 
                    null, new Object[]{"OK"}, "OK");
                    
            // Record the grade in the database
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO student_quiz_grades (student_id, quiz_id, score, submission_date) " +
                     "VALUES (?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE score = ?, submission_date = NOW()")) {
                ps.setInt(1, currentUserId);
                ps.setInt(2, quizId);
                ps.setDouble(3, score);
                ps.setDouble(4, score);
                ps.executeUpdate();
                
                // Refresh grades display
                loadStudentGrades(currentUserId);
            } catch (Exception ex) {
                showStyledMessageDialog(this, "Error saving grade:\n" + ex.getMessage(), 
                        "DB Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error retrieving quiz questions:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /* =========================================================
       Helper method to take an assignment
       ========================================================= */
       private void takeAssignment() {
        if (assignmentsList.getSelectedIndex() == -1) {
            showStyledMessageDialog(this, "Please select an assignment first.", "No Assignment Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selectedAssignment = assignmentsList.getSelectedValue();
        if (selectedAssignment.startsWith("No assignments")) {
            return;
        }
        
        // Extract assignment ID from the list item
        int assignmentId;
        try {
            assignmentId = Integer.parseInt(selectedAssignment.substring(0, selectedAssignment.indexOf(" -")).trim());
        } catch (Exception e) {
            showStyledMessageDialog(this, "Error parsing assignment ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if assignment has already been taken by this student
        String activityKey = "assignment_" + currentUserId + "_" + assignmentId;
        if (completedActivities.contains(activityKey)) {
            showStyledMessageDialog(this, "You have already completed this assignment.", 
                    "Assignment Already Taken", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Fetch assignment questions
        ArrayList<Question> questions = new ArrayList<>();
        String assignmentName = "";
        int score = 0; // Define score here, outside the try block
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // First, get the assignment name and grade
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(
                     "SELECT Assignment_Name, Assignment_Grade FROM assignments WHERE Assignment_id = ?")) {
                ps.setInt(1, assignmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        assignmentName = rs.getString(1);
                    } else {
                        showStyledMessageDialog(this, "Assignment not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            
            // Then get the questions
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(
                     """
                     SELECT question_id, question_text, answer_option_1, answer_option_2, 
                            answer_option_3, answer_option_4, correct_answer
                     FROM questions
                     WHERE assignment_id = ?
                     """)) {
                ps.setInt(1, assignmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Question q = new Question();
                        q.id = rs.getInt(1);
                        q.text = rs.getString(2);
                        q.options.add(rs.getString(3));
                        q.options.add(rs.getString(4));
                        
                        String option3 = rs.getString(5);
                        if (option3 != null && !option3.isEmpty()) {
                            q.options.add(option3);
                        }
                        
                        String option4 = rs.getString(6);
                        if (option4 != null && !option4.isEmpty()) {
                            q.options.add(option4);
                        }
                        
                        q.correctAnswer = rs.getInt(7);
                        questions.add(q);
                    }
                }
            }
            
            if (questions.isEmpty()) {
                showStyledMessageDialog(this, "This assignment has no questions.", "Empty Assignment", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Create the assignment taking UI
            score = showQuizDialog(assignmentName, questions); // assign to the score variable we defined earlier
            
            // Mark assignment as completed
            completedActivities.add(activityKey);
            
            // Show result
            JPanel resultPanel = new JPanel(new BorderLayout(0, 10));
            resultPanel.setBackground(DARK_BG);
            
            JLabel titleLabel = new JLabel("Assignment Results", JLabel.CENTER);
            titleLabel.setFont(HEADER_FONT);
            titleLabel.setForeground(TEAL_ACCENT);
            
            JLabel scoreLabel = new JLabel(String.format(
                "You scored %d out of %d points (%.1f%%)", 
                score, questions.size(), (100.0 * score / questions.size())),
                JLabel.CENTER);
            scoreLabel.setFont(SUBHEADER_FONT);
            scoreLabel.setForeground(TEXT_LIGHT);
            
            resultPanel.add(titleLabel, BorderLayout.NORTH);
            resultPanel.add(scoreLabel, BorderLayout.CENTER);
            
            showStyledOptionDialog(this, resultPanel, "Assignment Completed", 
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, 
                    null, new Object[]{"OK"}, "OK");
                    
            // Record the grade in the database
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO student_assignment_grades (student_id, assignment_id, score, submission_date) " +
                     "VALUES (?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE score = ?, submission_date = NOW()")) {
                ps.setInt(1, currentUserId);
                ps.setInt(2, assignmentId);
                ps.setDouble(3, score);
                ps.setDouble(4, score);
                ps.executeUpdate();
                
                // Refresh grades display
                loadStudentGrades(currentUserId);
            } catch (Exception ex) {
                showStyledMessageDialog(this, "Error saving grade:\n" + ex.getMessage(), 
                        "DB Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error retrieving assignment questions:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /* =========================================================
       Quiz/Assignment Question Class
       ========================================================= */
    private class Question {
        int id;
        String text;
        ArrayList<String> options = new ArrayList<>();
        int correctAnswer;
        int userAnswer = -1;
    }
    
    /* =========================================================
       Show quiz dialog with custom styling
       ========================================================= */
    private int showQuizDialog(String title, ArrayList<Question> questions) {
        JDialog quizDialog = new JDialog((JFrame)SwingUtilities.getWindowAncestor(this), title, true);
        quizDialog.setLayout(new BorderLayout());
        quizDialog.getContentPane().setBackground(DARK_BG);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PURPLE_PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEXT_LIGHT);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        quizDialog.add(headerPanel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(DARK_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        ButtonGroup[] groups = new ButtonGroup[questions.size()];
        JRadioButton[][] optionButtons = new JRadioButton[questions.size()][];
        
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            
            JPanel questionPanel = new JPanel(new BorderLayout(0, 10));
            questionPanel.setBackground(PANEL_BG);
            questionPanel.setBorder(BorderFactory.createCompoundBorder(
                createRoundedBorder(10, TEAL_ACCENT),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            
            JLabel questionLabel = new JLabel(String.format("<html><b>Question %d:</b> %s</html>", i+1, q.text));
            questionLabel.setFont(SUBHEADER_FONT);
            questionLabel.setForeground(TEXT_LIGHT);
            
            questionPanel.add(questionLabel, BorderLayout.NORTH);
            
            JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            optionsPanel.setBackground(PANEL_BG);
            
            groups[i] = new ButtonGroup();
            optionButtons[i] = new JRadioButton[q.options.size()];
            
            for (int j = 0; j < q.options.size(); j++) {
                optionButtons[i][j] = new JRadioButton(q.options.get(j));
                optionButtons[i][j].setFont(REGULAR_FONT);
                optionButtons[i][j].setBackground(PANEL_BG);
                optionButtons[i][j].setForeground(TEXT_LIGHT);
                optionButtons[i][j].setFocusPainted(false);
                final int questionIndex = i;
                final int answerIndex = j + 1;
                optionButtons[i][j].addActionListener(e -> questions.get(questionIndex).userAnswer = answerIndex);
                
                groups[i].add(optionButtons[i][j]);
                optionsPanel.add(optionButtons[i][j]);
            }
            
            questionPanel.add(optionsPanel, BorderLayout.CENTER);
            contentPanel.add(questionPanel);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        quizDialog.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPanel.setBackground(DARK_BG);
        
        JButton submitButton = createStyledButton("Submit");
        JButton cancelButton = createStyledButton("Cancel");
        
        cancelButton.addActionListener(e -> {
            if (showStyledConfirmDialog(quizDialog, "Are you sure you want to cancel? Your progress will be lost.", 
                    "Confirm Cancel", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                for (Question q : questions) {
                    q.userAnswer = -1;
                }
                quizDialog.dispose();
            }
        });
        
        submitButton.addActionListener(e -> {
            // Check if all questions are answered
            boolean allAnswered = true;
            for (Question q : questions) {
                if (q.userAnswer == -1) {
                    allAnswered = false;
                    break;
                }
            }
            
            if (!allAnswered && showStyledConfirmDialog(quizDialog, 
                    "You haven't answered all questions. Do you want to submit anyway?", 
                    "Confirm Submit", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
            
            quizDialog.dispose();
        });
        
        buttonsPanel.add(submitButton);
        buttonsPanel.add(cancelButton);
        
        quizDialog.add(buttonsPanel, BorderLayout.SOUTH);
        
        quizDialog.setSize(800, 600);
        quizDialog.setLocationRelativeTo(this);
        quizDialog.setVisible(true);
        
        // Calculate score
        int score = 0;
        for (Question q : questions) {
            if (q.userAnswer == q.correctAnswer) {
                score++;
            }
        }
        
        return score;
    }

    /* =========================================================
       Create a styled JButton
       ========================================================= */
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(REGULAR_FONT);
        btn.setBackground(TEAL_ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(TEAL_ACCENT.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        final JButton finalBtn = btn; // Create a final reference for the anonymous inner class
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                finalBtn.setBackground(TEAL_ACCENT.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                finalBtn.setBackground(TEAL_ACCENT);
            }
        });
        
        return btn;
    }

    /* =========================================================
       Create a rounded border
       ========================================================= */
    private Border createRoundedBorder(int radius, Color color) {
        return new CompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );
    }

    /* =========================================================
       Custom styled message dialog
       ========================================================= */
    private void showStyledMessageDialog(Component parent, Object message, String title, int messageType) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(messageType == JOptionPane.ERROR_MESSAGE ? Color.RED : TEAL_ACCENT);
        
        JLabel msgLabel;
        if (message instanceof Component) {
            msgLabel = null;
            panel.add((Component) message, BorderLayout.CENTER);
        } else {
            msgLabel = new JLabel("<html>" + message.toString() + "</html>", JLabel.CENTER);
            msgLabel.setFont(REGULAR_FONT);
            msgLabel.setForeground(TEXT_LIGHT);
            panel.add(msgLabel, BorderLayout.CENTER);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        Object[] options = {"OK"};
        JOptionPane pane = new JOptionPane(panel, messageType, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
        JDialog dialog = pane.createDialog(parent, title);
        
        dialog.setVisible(true);
    }
    
    /* =========================================================
       Custom styled confirm dialog
       ========================================================= */
    private int showStyledConfirmDialog(Component parent, Object message, String title, int optionType) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEAL_ACCENT);
        
        JLabel msgLabel;
        if (message instanceof Component) {
            msgLabel = null;
            panel.add((Component) message, BorderLayout.CENTER);
        } else {
            msgLabel = new JLabel("<html>" + message.toString() + "</html>", JLabel.CENTER);
            msgLabel.setFont(REGULAR_FONT);
            msgLabel.setForeground(TEXT_LIGHT);
            panel.add(msgLabel, BorderLayout.CENTER);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        Object[] options;
        if (optionType == JOptionPane.YES_NO_OPTION) {
            options = new Object[]{"Yes", "No"};
        } else if (optionType == JOptionPane.YES_NO_CANCEL_OPTION) {
            options = new Object[]{"Yes", "No", "Cancel"};
        } else {
            options = new Object[]{"OK", "Cancel"};
        }
        
        JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, optionType, null, options, options[0]);
        JDialog dialog = pane.createDialog(parent, title);
        
        dialog.setVisible(true);
        
        Object selectedValue = pane.getValue();
        if (selectedValue == null) {
            return JOptionPane.CLOSED_OPTION;
        }
        
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selectedValue)) {
                if (optionType == JOptionPane.YES_NO_OPTION || optionType == JOptionPane.YES_NO_CANCEL_OPTION) {
                    if (i == 0) return JOptionPane.YES_OPTION;
                    if (i == 1) return JOptionPane.NO_OPTION;
                    return JOptionPane.CANCEL_OPTION;
                } else {
                    return i;
                }
            }
        }
        
        return JOptionPane.CLOSED_OPTION;
    }
    
    /* =========================================================
       Custom styled option dialog
       ========================================================= */
    private int showStyledOptionDialog(Component parent, Object message, String title, 
                                       int optionType, int messageType, Icon icon, 
                                       Object[] options, Object initialValue) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEAL_ACCENT);
        
        if (message instanceof Component) {
            panel.add((Component) message, BorderLayout.CENTER);
        } else {
            JLabel msgLabel = new JLabel("<html>" + message.toString() + "</html>", JLabel.CENTER);
            msgLabel.setFont(REGULAR_FONT);
            msgLabel.setForeground(TEXT_LIGHT);
            panel.add(msgLabel, BorderLayout.CENTER);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JOptionPane pane = new JOptionPane(panel, messageType, optionType, icon, options, initialValue);
        JDialog dialog = pane.createDialog(parent, title);
        
        dialog.setVisible(true);
        
        Object selectedValue = pane.getValue();
        if (selectedValue == null) {
            return JOptionPane.CLOSED_OPTION;
        }
        
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selectedValue)) {
                return i;
            }
        }
        
        return JOptionPane.CLOSED_OPTION;
    }

    /* =========================================================
       View quizzes button action
       ========================================================= */
    private void viewQuizzesActionPerformed(java.awt.event.ActionEvent evt) {
        if (currentSelectedClassId == -1) {
            showStyledMessageDialog(this, "Please select a class first.", "No Class Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            ArrayList<String> quizzes = new ArrayList<>();
            
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT Quiz_id, Quiz_Name, Quiz_Grade FROM quizzes WHERE classes_id = ?")) {
                ps.setInt(1, currentSelectedClassId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        quizzes.add(String.format("%d - %s (%.1f points)", 
                                     rs.getInt(1), rs.getString(2), rs.getDouble(3)));
                    }
                }
            }
            
            if (quizzes.isEmpty()) {
                showStyledMessageDialog(this, "No quizzes found for this class.", 
                        "No Quizzes", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            JList<String> quizList = new JList<>(quizzes.toArray(new String[0]));
            quizList.setFont(REGULAR_FONT);
            quizList.setBackground(new Color(80, 90, 100));
            quizList.setForeground(TEXT_LIGHT);
            quizList.setSelectionBackground(TEAL_ACCENT);
            quizList.setSelectionForeground(Color.WHITE);
            
            JScrollPane scrollPane = new JScrollPane(quizList);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBackground(DARK_BG);
            
            JLabel titleLabel = new JLabel("Quizzes for Class ID: " + currentSelectedClassId, JLabel.CENTER);
            titleLabel.setFont(HEADER_FONT);
            titleLabel.setForeground(TEAL_ACCENT);
            
            panel.add(titleLabel, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            showStyledOptionDialog(this, panel, "View Quizzes", 
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
                    null, new Object[]{"Close"}, "Close");
            
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error retrieving quizzes:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================================================
       View assignments button action
       ========================================================= */
    private void viewAssignmentsActionPerformed(java.awt.event.ActionEvent evt) {
        if (currentSelectedClassId == -1) {
            showStyledMessageDialog(this, "Please select a class first.", "No Class Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            ArrayList<String> assignments = new ArrayList<>();
            
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT Assignment_id, Assignment_Name, Assignment_Grade FROM assignments WHERE classes_id = ?")) {
                ps.setInt(1, currentSelectedClassId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        assignments.add(String.format("%d - %s (%.1f points)", 
                                        rs.getInt(1), rs.getString(2), rs.getDouble(3)));
                    }
                }
            }
            
            if (assignments.isEmpty()) {
                showStyledMessageDialog(this, "No assignments found for this class.", 
                        "No Assignments", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            JList<String> assignmentList = new JList<>(assignments.toArray(new String[0]));
            assignmentList.setFont(REGULAR_FONT);
            assignmentList.setBackground(new Color(80, 90, 100));
            assignmentList.setForeground(TEXT_LIGHT);
            assignmentList.setSelectionBackground(TEAL_ACCENT);
            assignmentList.setSelectionForeground(Color.WHITE);
            
            JScrollPane scrollPane = new JScrollPane(assignmentList);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBackground(DARK_BG);
            
            JLabel titleLabel = new JLabel("Assignments for Class ID: " + currentSelectedClassId, JLabel.CENTER);
            titleLabel.setFont(HEADER_FONT);
            titleLabel.setForeground(TEAL_ACCENT);
            
            panel.add(titleLabel, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            showStyledOptionDialog(this, panel, "View Assignments", 
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
                    null, new Object[]{"Close"}, "Close");
            
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error retrieving assignments:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================================================
       ADD‑STUDENT
       ========================================================= */
    private void AddStudent_ButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(DARK_BG);
        
        JLabel titleLabel = new JLabel("Add New Student", JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEAL_ACCENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        fieldsPanel.setBackground(DARK_BG);
        
        JTextField idField = createStyledTextField();
        JTextField fnField = createStyledTextField();
        JTextField lnField = createStyledTextField();
        JTextField gdField = createStyledTextField();
        JTextField mailField = createStyledTextField();
        JTextField pwField = createStyledTextField();
        JTextField clsField = createStyledTextField();
        
        fieldsPanel.add(createStyledLabel("Student ID (int):"));
        fieldsPanel.add(idField);
        fieldsPanel.add(createStyledLabel("First Name:"));
        fieldsPanel.add(fnField);
        fieldsPanel.add(createStyledLabel("Last Name:"));
        fieldsPanel.add(lnField);
        fieldsPanel.add(createStyledLabel("Gender:"));
        fieldsPanel.add(gdField);
        fieldsPanel.add(createStyledLabel("University Email:"));
        fieldsPanel.add(mailField);
        fieldsPanel.add(createStyledLabel("Password:"));
        fieldsPanel.add(pwField);
        fieldsPanel.add(createStyledLabel("Class ID (int):"));
        fieldsPanel.add(clsField);
        
        p.add(titleLabel, BorderLayout.NORTH);
        p.add(fieldsPanel, BorderLayout.CENTER);
        
        Object[] options = {"Add", "Cancel"};
        
        int result = showStyledOptionDialog(this, p, "Add Student", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                null, options, options[0]);
        
        if (result != 0) return; // User clicked Cancel
        
        String sid = idField.getText().trim();
        String fn = fnField.getText().trim();
        String ln = lnField.getText().trim();
        String gd = gdField.getText().trim();
        String em = mailField.getText().trim();
        String pw = pwField.getText().trim();
        String cid = clsField.getText().trim();
        
        if (sid.isEmpty() || fn.isEmpty() || ln.isEmpty() || gd.isEmpty() || em.isEmpty() || pw.isEmpty()) {
            showStyledMessageDialog(this, "All fields except Class ID are required.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        final String sql = """
            INSERT INTO student
              (Std_ID, First_Name, Last_Name, Gender, Uni_Email, Log_cred, classes_id)
            VALUES (?,?,?,?,?,?,?)""";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(sid));
                ps.setString(2, fn);
                ps.setString(3, ln);
                ps.setString(4, gd);
                ps.setString(5, em);
                ps.setString(6, pw);
                
                if (cid.isBlank()) {
                    ps.setNull(7, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(7, Integer.parseInt(cid));
                }
                
                ps.executeUpdate();
                showStyledMessageDialog(this, "Student added successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error adding student:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================================================
       ADD‑INSTRUCTOR
       ========================================================= */
    private void AddInstructor_ButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(DARK_BG);
        
        JLabel titleLabel = new JLabel("Add New Instructor", JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEAL_ACCENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        fieldsPanel.setBackground(DARK_BG);
        
        JTextField idField = createStyledTextField();
        JTextField fnField = createStyledTextField();
        JTextField lnField = createStyledTextField();
        JTextField gdField = createStyledTextField();
        JTextField mailField = createStyledTextField();
        JTextField pwField = createStyledTextField();
        JTextField depField = createStyledTextField();
        
        fieldsPanel.add(createStyledLabel("Instructor ID (int):"));
        fieldsPanel.add(idField);
        fieldsPanel.add(createStyledLabel("First Name:"));
        fieldsPanel.add(fnField);
        fieldsPanel.add(createStyledLabel("Last Name:"));
        fieldsPanel.add(lnField);
        fieldsPanel.add(createStyledLabel("Gender:"));
        fieldsPanel.add(gdField);
        fieldsPanel.add(createStyledLabel("University Email:"));
        fieldsPanel.add(mailField);
        fieldsPanel.add(createStyledLabel("Password:"));
        fieldsPanel.add(pwField);
        fieldsPanel.add(createStyledLabel("Department:"));
        fieldsPanel.add(depField);
        
        p.add(titleLabel, BorderLayout.NORTH);
        p.add(fieldsPanel, BorderLayout.CENTER);
        
        Object[] options = {"Add", "Cancel"};
        
        int result = showStyledOptionDialog(this, p, "Add Instructor", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                null, options, options[0]);
        
        if (result != 0) return; // User clicked Cancel
        
        String iid = idField.getText().trim();
        String fn = fnField.getText().trim();
        String ln = lnField.getText().trim();
        String gd = gdField.getText().trim();
        String em = mailField.getText().trim();
        String pw = pwField.getText().trim();
        String dp = depField.getText().trim();
        
        if (iid.isEmpty() || fn.isEmpty() || ln.isEmpty() || gd.isEmpty() || em.isEmpty() || pw.isEmpty() || dp.isEmpty()) {
            showStyledMessageDialog(this, "All fields are required.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        final String sql = """
            INSERT INTO instructors
              (Inst_id, First_Name, Last_Name, Gender, Uni_Email,
               Log_cred, Department)
            VALUES (?,?,?,?,?,?,?)""";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(iid));
                ps.setString(2, fn);
                ps.setString(3, ln);
                ps.setString(4, gd);
                ps.setString(5, em);
                ps.setString(6, pw);
                ps.setString(7, dp);
                
                ps.executeUpdate();
                showStyledMessageDialog(this, "Instructor added successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error adding instructor:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================================================
       Create styled label
       ========================================================= */
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(REGULAR_FONT);
        label.setForeground(TEXT_LIGHT);
        return label;
    }

    /* =========================================================
       Create styled text field
       ========================================================= */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(REGULAR_FONT);
        field.setBackground(new Color(70, 70, 70));
        field.setForeground(TEXT_LIGHT);
        field.setCaretColor(TEXT_LIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, TEAL_ACCENT),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    /* =========================================================
       CREATE‑CLASS
       ========================================================= */
    private void CreateClass_ButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(DARK_BG);
        
        JLabel titleLabel = new JLabel("Create New Class", JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEAL_ACCENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel annLabel = new JLabel("Announcement (optional):");
        annLabel.setFont(REGULAR_FONT);
        annLabel.setForeground(TEXT_LIGHT);
        
        JTextArea annField = new JTextArea(5, 30);
        annField.setFont(REGULAR_FONT);
        annField.setBackground(new Color(70, 70, 70));
        annField.setForeground(TEXT_LIGHT);
        annField.setCaretColor(TEXT_LIGHT);
        annField.setLineWrap(true);
        annField.setWrapStyleWord(true);
        annField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, TEAL_ACCENT),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JScrollPane sp = new JScrollPane(annField);
        sp.setBorder(null);
        
        JPanel contentPanel = new JPanel(new BorderLayout(5, 10));
        contentPanel.setBackground(DARK_BG);
        contentPanel.add(annLabel, BorderLayout.NORTH);
        contentPanel.add(sp, BorderLayout.CENTER);
        
        p.add(titleLabel, BorderLayout.NORTH);
        p.add(contentPanel, BorderLayout.CENTER);
        
        Object[] options = {"Create", "Cancel"};
        
        int result = showStyledOptionDialog(this, p, "Create Class", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                null, options, options[0]);
        
        if (result != 0) return; // User clicked Cancel
        
        String announcement = annField.getText().trim();
        
        final String sql = "INSERT INTO classes (announcement) VALUES (?)";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)
            ;
                 PreparedStatement ps = c.prepareStatement(sql)) {
                
                if (announcement.isEmpty()) {
                    ps.setNull(1, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(1, announcement);
                }
                
                ps.executeUpdate();
                showStyledMessageDialog(this, "Class created successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error creating class:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================================================
       CREATE‑COURSE
       ========================================================= */
    private void CreateCourse_ButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(DARK_BG);
        
        JLabel titleLabel = new JLabel("Create New Course", JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEAL_ACCENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        fieldsPanel.setBackground(DARK_BG);
        
        JTextField nameField = createStyledTextField();
        JTextField crField = createStyledTextField();
        
        fieldsPanel.add(createStyledLabel("Course Name:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(createStyledLabel("Credit Hours (int):"));
        fieldsPanel.add(crField);
        
        p.add(titleLabel, BorderLayout.NORTH);
        p.add(fieldsPanel, BorderLayout.CENTER);
        
        Object[] options = {"Create", "Cancel"};
        
        int result = showStyledOptionDialog(this, p, "Create Course", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                null, options, options[0]);
        
        if (result != 0) return; // User clicked Cancel
        
        String nm = nameField.getText().trim();
        String cr = crField.getText().trim();
        
        if (nm.isEmpty() || cr.isEmpty()) {
            showStyledMessageDialog(this, "Course name and credit hours are required.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        final String sql = "INSERT INTO courses (courses_Name, Credit_HR) VALUES (?,?)";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, nm);
                ps.setInt(2, Integer.parseInt(cr));
                
                ps.executeUpdate();
                showStyledMessageDialog(this, "Course created successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error creating course:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================================================
       Link Course-Class
       ========================================================= */
    private void LinkCourseClassActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            // Get courses
            ArrayList<Integer> courseIds = new ArrayList<>();
            ArrayList<String> courseNames = new ArrayList<>();
            
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT courses_id, courses_Name FROM courses")) {
                while (rs.next()) {
                    courseIds.add(rs.getInt(1));
                    courseNames.add(rs.getInt(1) + " - " + rs.getString(2));
                }
            }
            
            if (courseIds.isEmpty()) {
                showStyledMessageDialog(this, "No courses available to link.", 
                        "No Courses", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get classes
            ArrayList<Integer> classIds = new ArrayList<>();
            ArrayList<String> classDescs = new ArrayList<>();
            
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT Classes_id, announcement FROM classes")) {
                while (rs.next()) {
                    classIds.add(rs.getInt(1));
                    String ann = rs.getString(2);
                    classDescs.add(rs.getInt(1) + (ann != null ? " - " + 
                            (ann.length() > 30 ? ann.substring(0, 27) + "..." : ann) : ""));
                }
            }
            
            if (classIds.isEmpty()) {
                showStyledMessageDialog(this, "No classes available to link.", 
                        "No Classes", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create styled combo boxes
            JComboBox<String> courseBox = new JComboBox<>(courseNames.toArray(new String[0]));
            courseBox.setFont(REGULAR_FONT);
            courseBox.setBackground(new Color(70, 70, 70));
            courseBox.setForeground(TEXT_LIGHT);
            
            JComboBox<String> classBox = new JComboBox<>(classDescs.toArray(new String[0]));
            classBox.setFont(REGULAR_FONT);
            classBox.setBackground(new Color(70, 70, 70));
            classBox.setForeground(TEXT_LIGHT);
            
            JPanel p = new JPanel(new BorderLayout(10, 10));
            p.setBackground(DARK_BG);
            
            JLabel titleLabel = new JLabel("Link Course to Class", JLabel.CENTER);
            titleLabel.setFont(HEADER_FONT);
            titleLabel.setForeground(TEAL_ACCENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            
            JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            fieldsPanel.setBackground(DARK_BG);
            
            fieldsPanel.add(createStyledLabel("Select Course:"));
            fieldsPanel.add(courseBox);
            fieldsPanel.add(createStyledLabel("Select Class:"));
            fieldsPanel.add(classBox);
            
            p.add(titleLabel, BorderLayout.NORTH);
            p.add(fieldsPanel, BorderLayout.CENTER);
            
            Object[] options = {"Link", "Cancel"};
            
            int result = showStyledOptionDialog(this, p, "Link Course to Class", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                    null, options, options[0]);
            
            if (result != 0) return; // User clicked Cancel
            
            int courseId = courseIds.get(courseBox.getSelectedIndex());
            int classId = classIds.get(classBox.getSelectedIndex());
            
            // Check if link already exists
            boolean exists = false;
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT 1 FROM course_class WHERE course_id = ? AND class_id = ?")) {
                ps.setInt(1, courseId);
                ps.setInt(2, classId);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }
            
            if (exists) {
                showStyledMessageDialog(this, "This course is already linked to this class.", 
                        "Link Exists", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create the link
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO course_class (course_id, class_id) VALUES (?, ?)")) {
                ps.setInt(1, courseId);
                ps.setInt(2, classId);
                ps.executeUpdate();
                showStyledMessageDialog(this, "Course has been linked to class successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error linking course to class:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================================================
       Link Instructor-Class
       ========================================================= */
    private void LinkInstructorClassActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            // Get instructors
            ArrayList<Integer> instIds = new ArrayList<>();
            ArrayList<String> instNames = new ArrayList<>();
            
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery(
                         "SELECT Inst_id, CONCAT(First_Name, ' ', Last_Name) FROM instructors")) {
                while (rs.next()) {
                    instIds.add(rs.getInt(1));
                    instNames.add(rs.getInt(1) + " - " + rs.getString(2));
                }
            }
            
            if (instIds.isEmpty()) {
                showStyledMessageDialog(this, "No instructors available to link.", 
                        "No Instructors", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get classes
            ArrayList<Integer> classIds = new ArrayList<>();
            ArrayList<String> classDescs = new ArrayList<>();
            
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT Classes_id, announcement FROM classes")) {
                while (rs.next()) {
                    classIds.add(rs.getInt(1));
                    String ann = rs.getString(2);
                    classDescs.add(rs.getInt(1) + (ann != null ? " - " + 
                            (ann.length() > 30 ? ann.substring(0, 27) + "..." : ann) : ""));
                }
            }
            
            if (classIds.isEmpty()) {
                showStyledMessageDialog(this, "No classes available to link.", 
                        "No Classes", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create styled combo boxes
            JComboBox<String> instBox = new JComboBox<>(instNames.toArray(new String[0]));
            instBox.setFont(REGULAR_FONT);
            instBox.setBackground(new Color(70, 70, 70));
            instBox.setForeground(TEXT_LIGHT);
            
            JComboBox<String> classBox = new JComboBox<>(classDescs.toArray(new String[0]));
            classBox.setFont(REGULAR_FONT);
            classBox.setBackground(new Color(70, 70, 70));
            classBox.setForeground(TEXT_LIGHT);
            
            JPanel p = new JPanel(new BorderLayout(10, 10));
            p.setBackground(DARK_BG);
            
            JLabel titleLabel = new JLabel("Link Instructor to Class", JLabel.CENTER);
            titleLabel.setFont(HEADER_FONT);
            titleLabel.setForeground(TEAL_ACCENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            
            JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            fieldsPanel.setBackground(DARK_BG);
            
            fieldsPanel.add(createStyledLabel("Select Instructor:"));
            fieldsPanel.add(instBox);
            fieldsPanel.add(createStyledLabel("Select Class:"));
            fieldsPanel.add(classBox);
            
            p.add(titleLabel, BorderLayout.NORTH);
            p.add(fieldsPanel, BorderLayout.CENTER);
            
            Object[] options = {"Link", "Cancel"};
            
            int result = showStyledOptionDialog(this, p, "Link Instructor to Class", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                    null, options, options[0]);
            
            if (result != 0) return; // User clicked Cancel
            
            int instId = instIds.get(instBox.getSelectedIndex());
            int classId = classIds.get(classBox.getSelectedIndex());
            
            // Check if link already exists
            boolean exists = false;
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT 1 FROM instructor_class WHERE instructor_id = ? AND class_id = ?")) {
                ps.setInt(1, instId);
                ps.setInt(2, classId);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }
            
            if (exists) {
                showStyledMessageDialog(this, "This instructor is already linked to this class." , "Link Exists", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create the link
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO instructor_class (instructor_id, class_id) VALUES (?, ?)")) {
                ps.setInt(1, instId);
                ps.setInt(2, classId);
                ps.executeUpdate();
                showStyledMessageDialog(this, "Instructor has been linked to class successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error linking instructor to class:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================================================
       Assign Instructor-Course-Class (NEW)
       ========================================================= */
    private void AssignInstructorCourseClassActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            // First, ensure the instructor_class_course table exists
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement s = c.createStatement()) {
                // Check if table exists
                boolean tableExists = false;
                try (ResultSet rs = c.getMetaData().getTables(null, null, "instructor_class_course", null)) {
                    tableExists = rs.next();
                }
                
                // Create table if it doesn't exist
                if (!tableExists) {
                    s.execute("""
                        CREATE TABLE instructor_class_course (
                            instructor_id INT,
                            class_id INT,
                            course_id INT,
                            PRIMARY KEY (instructor_id, class_id, course_id),
                            FOREIGN KEY (instructor_id) REFERENCES instructors(Inst_id),
                            FOREIGN KEY (class_id) REFERENCES classes(Classes_id),
                            FOREIGN KEY (course_id) REFERENCES courses(courses_id)
                        )
                    """);
                }
            }
            
            // Get instructors
            ArrayList<Integer> instIds = new ArrayList<>();
            ArrayList<String> instNames = new ArrayList<>();
            
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery(
                         "SELECT Inst_id, CONCAT(First_Name, ' ', Last_Name) FROM instructors")) {
                while (rs.next()) {
                    instIds.add(rs.getInt(1));
                    instNames.add(rs.getInt(1) + " - " + rs.getString(2));
                }
            }
            
            if (instIds.isEmpty()) {
                showStyledMessageDialog(this, "No instructors available to assign.", 
                        "No Instructors", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get courses
            ArrayList<Integer> courseIds = new ArrayList<>();
            ArrayList<String> courseNames = new ArrayList<>();
            
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT courses_id, courses_Name FROM courses")) {
                while (rs.next()) {
                    courseIds.add(rs.getInt(1));
                    courseNames.add(rs.getInt(1) + " - " + rs.getString(2));
                }
            }
            
            if (courseIds.isEmpty()) {
                showStyledMessageDialog(this, "No courses available to assign.", 
                        "No Courses", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get classes
            ArrayList<Integer> classIds = new ArrayList<>();
            ArrayList<String> classDescs = new ArrayList<>();
            
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement s = c.createStatement();
                 ResultSet rs = s.executeQuery("SELECT Classes_id, announcement FROM classes")) {
                while (rs.next()) {
                    classIds.add(rs.getInt(1));
                    String ann = rs.getString(2);
                    classDescs.add(rs.getInt(1) + (ann != null ? " - " + 
                            (ann.length() > 30 ? ann.substring(0, 27) + "..." : ann) : " - No announcement"));
                }
            }
            
            if (classIds.isEmpty()) {
                showStyledMessageDialog(this, "No classes available to assign.", 
                        "No Classes", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create styled combo boxes
            JComboBox<String> instBox = new JComboBox<>(instNames.toArray(new String[0]));
            instBox.setFont(REGULAR_FONT);
            instBox.setBackground(new Color(70, 70, 70));
            instBox.setForeground(TEXT_LIGHT);
            
            JComboBox<String> courseBox = new JComboBox<>(courseNames.toArray(new String[0]));
            courseBox.setFont(REGULAR_FONT);
            courseBox.setBackground(new Color(70, 70, 70));
            courseBox.setForeground(TEXT_LIGHT);
            
            JComboBox<String> classBox = new JComboBox<>(classDescs.toArray(new String[0]));
            classBox.setFont(REGULAR_FONT);
            classBox.setBackground(new Color(70, 70, 70));
            classBox.setForeground(TEXT_LIGHT);
            
            JPanel p = new JPanel(new BorderLayout(10, 10));
            p.setBackground(DARK_BG);
            
            JLabel titleLabel = new JLabel("Assign Instructor to Course & Class", JLabel.CENTER);
            titleLabel.setFont(HEADER_FONT);
            titleLabel.setForeground(TEAL_ACCENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            
            JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            fieldsPanel.setBackground(DARK_BG);
            
            fieldsPanel.add(createStyledLabel("Select Instructor:"));
            fieldsPanel.add(instBox);
            fieldsPanel.add(createStyledLabel("Select Course:"));
            fieldsPanel.add(courseBox);
            fieldsPanel.add(createStyledLabel("Select Class:"));
            fieldsPanel.add(classBox);
            
            p.add(titleLabel, BorderLayout.NORTH);
            p.add(fieldsPanel, BorderLayout.CENTER);
            
            Object[] options = {"Assign", "Cancel"};
            
            int result = showStyledOptionDialog(this, p, "Assign Instructor", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                    null, options, options[0]);
            
            if (result != 0) return; // User clicked Cancel
            
            int instId = instIds.get(instBox.getSelectedIndex());
            int courseId = courseIds.get(courseBox.getSelectedIndex());
            int classId = classIds.get(classBox.getSelectedIndex());
            
            // Check if assignment already exists
            boolean exists = false;
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM instructor_class_course WHERE instructor_id = ? AND class_id = ? AND course_id = ?")) {
                ps.setInt(1, instId);
                ps.setInt(2, classId);
                ps.setInt(3, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }
            
            if (exists) {
                showStyledMessageDialog(this, "This teaching assignment already exists.", 
                        "Assignment Exists", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Verify that course and class are linked
            boolean courseClassLinked = false;
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM course_class WHERE course_id = ? AND class_id = ?")) {
                ps.setInt(1, courseId);
                ps.setInt(2, classId);
                try (ResultSet rs = ps.executeQuery()) {
                    courseClassLinked = rs.next();
                }
            }
            
            if (!courseClassLinked) {
                if (showStyledConfirmDialog(this, 
                        "This course is not linked to this class yet. Would you like to link them now?", 
                        "Link Required", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    
                    try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                         PreparedStatement ps = c.prepareStatement(
                             "INSERT INTO course_class (course_id, class_id) VALUES (?, ?)")) {
                        ps.setInt(1, courseId);
                        ps.setInt(2, classId);
                        ps.executeUpdate();
                    }
                } else {
                    return;
                }
            }
            
            // Create the teaching assignment
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO instructor_class_course (instructor_id, class_id, course_id) VALUES (?, ?, ?)")) {
                ps.setInt(1, instId);
                ps.setInt(2, classId);
                ps.setInt(3, courseId);
                ps.executeUpdate();
                
                // Also add to instructor_class for compatibility
                try (PreparedStatement ps2 = c.prepareStatement(
                     "INSERT IGNORE INTO instructor_class (instructor_id, class_id) VALUES (?, ?)")) {
                    ps2.setInt(1, instId);
                    ps2.setInt(2, classId);
                    ps2.executeUpdate();
                }
                
                showStyledMessageDialog(this, "Instructor has been assigned to teach this course for this class!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error assigning instructor:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================================================
       fetch and display the student's courses after login
       ========================================================= */
    private void loadStudentCourses(int studentId) {
        currentUserId = studentId;
        coursesModel.clear();
        quizzesModel.clear();
        assignmentsModel.clear();
        
        final String sql = """
            SELECT c.courses_Name, c.courses_id, cl.announcement, cl.Classes_id
            FROM student s
            JOIN classes cl ON s.classes_id = cl.Classes_id
            JOIN course_class cc ON cl.Classes_id = cc.class_id
            JOIN courses c ON cc.course_id = c.courses_id
            WHERE s.Std_ID = ?
        """;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String courseName = rs.getString(1);
                        coursesModel.addElement(courseName);
                        
                        // Set the announcement for the student's class
                        String announcement = rs.getString(3);
                        if (announcement != null && !announcement.isEmpty()) {
                            annArea.setText(announcement);
                        } else {
                            annArea.setText("No announcements available.");
                        }
                    }
                }
            }
            
            if (coursesModel.isEmpty()) coursesModel.addElement("No courses found");
        } catch (Exception ex) {
            coursesModel.clear();
            coursesModel.addElement("Error fetching courses: " + ex.getMessage());
        }
    }

    /* =========================================================
       Load assignments and quizzes for selected course
       ========================================================= */
    private void loadCourseContent(String courseName) {
        assignmentsModel.clear();
        quizzesModel.clear();
        quizIdToName.clear();
        assignmentIdToName.clear();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // First get class ID for this student and course
            int classId = -1;
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement("""
                    SELECT cl.Classes_id
                    FROM student s
                    JOIN classes cl ON s.classes_id = cl.Classes_id
                    JOIN course_class cc ON cl.Classes_id = cc.class_id
                    JOIN courses c ON cc.course_id = c.courses_id
                    WHERE s.Std_ID = ? AND c.courses_Name = ?
                 """)) {
                ps.setInt(1, currentUserId);
                ps.setString(2, courseName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        classId = rs.getInt(1);
                    }
                }
            }
            
            if (classId == -1) {
                assignmentsModel.addElement("Could not find class");
                quizzesModel.addElement("Could not find class");
                return;
            }
            
            // Load assignments
            final String assignmentSql = """
                SELECT a.Assignment_id, a.Assignment_Name, a.Assignment_Grade
                FROM assignments a
                WHERE a.classes_id = ?
            """;
            
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(assignmentSql)) {
                ps.setInt(1, classId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String name = rs.getString(2);
                        double grade = rs.getDouble(3);
                        String assignmentText = id + " - " + name + " (" + grade + " pts)";
                        
                        // Check if this assignment has already been taken
                        String activityKey = "assignment_" + currentUserId + "_" + id;
                        if (completedActivities.contains(activityKey)) {
                            assignmentText += " ✓"; // Mark completed assignments
                        }
                        
                        assignmentsModel.addElement(assignmentText);
                        assignmentIdToName.put(id, name);
                    }
                }
            }
            
            if (assignmentsModel.isEmpty()) assignmentsModel.addElement("No assignments");
            
            // Load quizzes
            final String quizSql = """
                SELECT q.Quiz_id, q.Quiz_Name, q.Quiz_Grade
                FROM quizzes q
                WHERE q.classes_id = ?
            """;
            
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(quizSql)) {
                ps.setInt(1, classId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String name = rs.getString(2);
                        double grade = rs.getDouble(3);
                        String quizText = id + " - " + name + " (" + grade + " pts)";
                        
                        // Check if this quiz has already been taken
                        String activityKey = "quiz_" + currentUserId + "_" + id;
                        if (completedActivities.contains(activityKey)) {
                            quizText += " ✓"; // Mark completed quizzes
                        }
                        
                        quizzesModel.addElement(quizText);
                        quizIdToName.put(id, name);
                    }
                }
            }
            
            if (quizzesModel.isEmpty()) quizzesModel.addElement("No quizzes");
            
        } catch (Exception ex) {
            assignmentsModel.clear();
            quizzesModel.clear();
            assignmentsModel.addElement("Error loading assignments");
            quizzesModel.addElement("Error loading quizzes");
        }
    }

    /* =========================================================
       Load classes for instructor with announcements
       ========================================================= */
    private void loadInstructorClasses(int instId) {
        currentUserId = instId;
        instructorClassesModel.clear();
        instructorCoursesModel.clear();
        
        // First, load regular instructor-class assignments
        final String sql = """
            SELECT c.Classes_id, c.announcement 
            FROM instructor_class ic
            JOIN classes c ON ic.class_id = c.Classes_id
            WHERE ic.instructor_id = ?""";
    
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, instId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String announcement = rs.getString(2);
                        if (announcement == null) announcement = "No announcement";
                        // Truncate announcement if too long
                        if (announcement.length() > 50) {
                            announcement = announcement.substring(0, 47) + "...";
                        }
                        String classInfo = "Class ID: " + rs.getInt(1) 
                                        + " - " + announcement;
                        instructorClassesModel.addElement(classInfo);
                    }
                }
            }
            
            // Next, check if instructor_class_course table exists and load from there
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                boolean tableExists = false;
                try (ResultSet rs = cn.getMetaData().getTables(null, null, "instructor_class_course", null)) {
                    tableExists = rs.next();
                }
                
                if (tableExists) {
                    // Load instructor-course-class assignments
                    final String courseSql = """
                        SELECT c.courses_id, c.courses_Name, cl.Classes_id
                        FROM instructor_class_course icc
                        JOIN courses c ON icc.course_id = c.courses_id
                        JOIN classes cl ON icc.class_id = cl.Classes_id
                        WHERE icc.instructor_id = ?
                    """;
                    
                    try (PreparedStatement ps = cn.prepareStatement(courseSql)) {
                        ps.setInt(1, instId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String courseInfo = "Course: " + rs.getString(2) + 
                                                   " (ID: " + rs.getInt(1) + ") - " +
                                                   "Class ID: " + rs.getInt(3);
                                instructorCoursesModel.addElement(courseInfo);
                            }
                        }
                    }
                }
            }
            
            if (instructorClassesModel.isEmpty()) {
                instructorClassesModel.addElement("No classes assigned");
            }
            
            if (instructorCoursesModel.isEmpty()) {
                instructorCoursesModel.addElement("No course-class assignments");
            }
        } catch (Exception ex) {
            instructorClassesModel.clear();
            instructorClassesModel.addElement("Error loading classes: " + ex.getMessage());
            
            instructorCoursesModel.clear();
            instructorCoursesModel.addElement("Error loading courses: " + ex.getMessage());
        }
    }

    /* =========================================================
       Instructor functionality - Edit Announcement
       ========================================================= */
    private void editAnnouncementActionPerformed(java.awt.event.ActionEvent evt) {
        if (currentSelectedClassId == -1) {
            showStyledMessageDialog(this, "Please select a class first.", 
                    "No Class Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Get current announcement
            String currentAnnouncement = "";
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT announcement FROM classes WHERE Classes_id = ?")) {
                ps.setInt(1, currentSelectedClassId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String ann = rs.getString(1);
                        if (ann != null) currentAnnouncement = ann;
                    }
                }
            }
            
            // Create edit dialog
            JPanel p = new JPanel(new BorderLayout(10, 10));
            p.setBackground(DARK_BG);
            
            JLabel titleLabel = new JLabel("Edit Announcement", JLabel.CENTER);
            titleLabel.setFont(HEADER_FONT);
            titleLabel.setForeground(TEAL_ACCENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            
            JLabel infoLabel = new JLabel("Editing announcement for Class ID: " + currentSelectedClassId);
            infoLabel.setFont(REGULAR_FONT);
            infoLabel.setForeground(TEXT_LIGHT);
            
            JTextArea annField = new JTextArea(10, 40);
            annField.setText(currentAnnouncement);
            annField.setFont(REGULAR_FONT);
            annField.setBackground(new Color(70, 70, 70));
            annField.setForeground(TEXT_LIGHT);
            annField.setCaretColor(TEXT_LIGHT);
            annField.setLineWrap(true);
            annField.setWrapStyleWord(true);
            annField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL_ACCENT, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            
            JScrollPane scrollPane = new JScrollPane(annField);
            scrollPane.setBorder(null);
            
            JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
            headerPanel.setBackground(DARK_BG);
            headerPanel.add(titleLabel, BorderLayout.NORTH);
            headerPanel.add(infoLabel, BorderLayout.CENTER);
            
            p.add(headerPanel, BorderLayout.NORTH);
            p.add(scrollPane, BorderLayout.CENTER);
            
            Object[] options = {"Save", "Cancel"};
            
            int result = showStyledOptionDialog(this, p, "Edit Announcement", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                    null, options, options[0]);
            
            if (result != 0) return; // User clicked Cancel
            
            // Update announcement
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                         "UPDATE classes SET announcement = ? WHERE Classes_id = ?")) {
                ps.setString(1, annField.getText().trim());
                ps.setInt(2, currentSelectedClassId);
                ps.executeUpdate();
                showStyledMessageDialog(this, "Announcement updated successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh class list
                loadInstructorClasses(currentUserId);
            }
            
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error updating announcement:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================================================
       Instructor functionality - Create Quiz
       ========================================================= */
    private void createQuizActionPerformed(java.awt.event.ActionEvent evt) {
        if (currentSelectedClassId == -1) {
            showStyledMessageDialog(this, "Please select a class first.", 
                    "No Class Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get quiz name and grade
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(DARK_BG);
        
        JLabel titleLabel = new JLabel("Create New Quiz", JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEAL_ACCENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        fieldsPanel.setBackground(DARK_BG);
        
        JTextField nameField = createStyledTextField();
        JTextField gradeField = createStyledTextField();
        gradeField.setText("100.00");
        
        fieldsPanel.add(createStyledLabel("Quiz Name:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(createStyledLabel("Maximum Grade:"));
        fieldsPanel.add(gradeField);
        
        p.add(titleLabel, BorderLayout.NORTH);
        p.add(fieldsPanel, BorderLayout.CENTER);
        
        Object[] options = {"Create", "Cancel"};
        
        int result = showStyledOptionDialog(this, p, "Create Quiz", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                null, options, options[0]);
        
        if (result != 0) return; // User clicked Cancel
        
        String name = nameField.getText().trim();
        String grade = gradeField.getText().trim();
        
        if (name.isEmpty()) {
            showStyledMessageDialog(this, "Quiz name is required.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Create quiz
            int quizId;
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO quizzes (Quiz_Name, Quiz_Grade, classes_id) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setBigDecimal(2, new java.math.BigDecimal(grade));
                ps.setInt(3, currentSelectedClassId);
                ps.executeUpdate();
                
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        quizId = keys.getInt(1);
                        showStyledMessageDialog(this, "Quiz created successfully!", 
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Option to add questions
                        if (showStyledConfirmDialog(this, 
                                "Would you like to add questions to this quiz?", 
                                "Add Questions", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            addQuestionsToQuiz(quizId);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error creating quiz:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /* =========================================================
       Add questions to quiz
       ========================================================= */
    private void addQuestionsToQuiz(int quizId) {
        boolean continueAdding = true;
        int questionCount = 0;
        
        while(continueAdding && questionCount < 3) {
            JPanel p = new JPanel(new BorderLayout(10, 10));
            p.setBackground(DARK_BG);
            
            JLabel titleLabel = new JLabel("Add Question " + (questionCount + 1), JLabel.CENTER);
            titleLabel.setFont(HEADER_FONT);
            titleLabel.setForeground(TEAL_ACCENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            
            JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            fieldsPanel.setBackground(DARK_BG);
            
            JTextField questionField = createStyledTextField();
            JTextField option1Field = createStyledTextField();
            JTextField option2Field = createStyledTextField();
            JTextField option3Field = createStyledTextField();
            JTextField option4Field = createStyledTextField();
            
            JComboBox<String> correctBox = new JComboBox<>(
                    new String[]{"Option 1", "Option 2", "Option 3", "Option 4"});
            correctBox.setFont(REGULAR_FONT);
            correctBox.setBackground(new Color(70, 70, 70));
            correctBox.setForeground(TEXT_LIGHT);
            
            fieldsPanel.add(createStyledLabel("Question:"));
            fieldsPanel.add(questionField);
            fieldsPanel.add(createStyledLabel("Option 1:"));
            fieldsPanel.add(option1Field);
            fieldsPanel.add(createStyledLabel("Option 2:"));
            fieldsPanel.add(option2Field);
            fieldsPanel.add(createStyledLabel("Option 3 (optional):"));
            fieldsPanel.add(option3Field);
            fieldsPanel.add(createStyledLabel("Option 4 (optional):"));
            fieldsPanel.add(option4Field);
            fieldsPanel.add(createStyledLabel("Correct Answer:"));
            fieldsPanel.add(correctBox);
            
            p.add(titleLabel, BorderLayout.NORTH);
            p.add(fieldsPanel, BorderLayout.CENTER);
            
            Object[] options = {"Add", "Cancel"};
            
            int result = showStyledOptionDialog(this, p, "Add Question", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                    null, options, options[0]);
            
            if (result != 0) {
                continueAdding = false;
                continue;
            }
            
            String question = questionField.getText().trim();
            String opt1 = option1Field.getText().trim();
            String opt2 = option2Field.getText().trim();
            String opt3 = option3Field.getText().trim();
            String opt4 = option4Field.getText().trim();
            int correct = correctBox.getSelectedIndex() + 1;
            
            if (question.isEmpty() || opt1.isEmpty() || opt2.isEmpty()) {
                showStyledMessageDialog(this, 
                        "Question, Option 1, and Option 2 are required.", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            try {
                try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = c.prepareStatement(
                         """
                         INSERT INTO questions 
                           (question_text, answer_option_1, answer_option_2, 
                            answer_option_3, answer_option_4, correct_answer, quiz_id)
                         VALUES (?, ?, ?, ?, ?, ?, ?)
                         """)) {
                    ps.setString(1, question);
                    ps.setString(2, opt1);
                    ps.setString(3, opt2);
                    ps.setString(4, opt3.isEmpty() ? null : opt3);
                    ps.setString(5, opt4.isEmpty() ? null : opt4);
                    ps.setInt(6, correct);
                    ps.setInt(7, quizId);
                    ps.executeUpdate();
                    questionCount++;
                    
                    if (questionCount >= 3) {
                        showStyledMessageDialog(this, 
                                "You've reached the maximum number of questions (3) for this quiz.",
                                "Maximum Reached", JOptionPane.INFORMATION_MESSAGE);
                        continueAdding = false;
                    } else {
                        continueAdding = showStyledConfirmDialog(this, 
                                "Question added successfully. Add another question?", 
                                "Add Another", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                    }
                }
            } catch (Exception ex) {
                showStyledMessageDialog(this, "Error adding question:\n" + ex.getMessage(), 
                        "DB Error", JOptionPane.ERROR_MESSAGE);
                continueAdding = false;
            }
        }
    }

    /* =========================================================
       Instructor functionality - Create Assignment
       ========================================================= */
    private void createAssignmentActionPerformed(java.awt.event.ActionEvent evt) {
        if (currentSelectedClassId == -1) {
            showStyledMessageDialog(this, "Please select a class first.", 
                    "No Class Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get assignment name and grade
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(DARK_BG);
        
        JLabel titleLabel = new JLabel("Create New Assignment", JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEAL_ACCENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        fieldsPanel.setBackground(DARK_BG);
        
        JTextField nameField = createStyledTextField();
        JTextField gradeField = createStyledTextField();
        gradeField.setText("100.00");
        
        fieldsPanel.add(createStyledLabel("Assignment Name:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(createStyledLabel("Maximum Grade:"));
        fieldsPanel.add(gradeField);
        
        p.add(titleLabel, BorderLayout.NORTH);
        p.add(fieldsPanel, BorderLayout.CENTER);
        
        Object[] options = {"Create", "Cancel"};
        
        int result = showStyledOptionDialog(this, p, "Create Assignment", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                null, options, options[0]);
        
        if (result != 0) return; // User clicked Cancel
        
        String name = nameField.getText().trim();
        String grade = gradeField.getText().trim();
        
        if (name.isEmpty()) {
            showStyledMessageDialog(this, "Assignment name is required.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Create assignment
            int assignmentId;
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO assignments (Assignment_Name, Assignment_Grade, classes_id) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setBigDecimal(2, new java.math.BigDecimal(grade));
                ps.setInt(3, currentSelectedClassId);
                ps.executeUpdate();
                
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        assignmentId = keys.getInt(1);
                        showStyledMessageDialog(this, "Assignment created successfully!", 
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Option to add questions
                        if (showStyledConfirmDialog(this, 
                                "Would you like to add questions to this assignment?", 
                                "Add Questions", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            addQuestionsToAssignment(assignmentId);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            showStyledMessageDialog(this, "Error creating assignment:\n" + ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /* =========================================================
       Add questions to assignment
       ========================================================= */
    private void addQuestionsToAssignment(int assignmentId) {
        boolean continueAdding = true;
        int questionCount = 0;
        
        while(continueAdding && questionCount < 5) {
            JPanel p = new JPanel(new BorderLayout(10, 10));
            p.setBackground(DARK_BG);
            
            JLabel titleLabel = new JLabel("Add Question " + (questionCount + 1), JLabel.CENTER);
            titleLabel.setFont(HEADER_FONT);
            titleLabel.setForeground(TEAL_ACCENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            
            JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            fieldsPanel.setBackground(DARK_BG);
            
            JTextField questionField = createStyledTextField();
            JTextField option1Field = createStyledTextField();
            JTextField option2Field = createStyledTextField();
            JTextField option3Field = createStyledTextField();
            JTextField option4Field = createStyledTextField();
            
            JComboBox<String> correctBox = new JComboBox<>(
                    new String[]{"Option 1", "Option 2", "Option 3", "Option 4"});
            correctBox.setFont(REGULAR_FONT);
            correctBox.setBackground(new Color(70, 70, 70));
            correctBox.setForeground(TEXT_LIGHT);
            
            fieldsPanel.add(createStyledLabel("Question:"));
            fieldsPanel.add(questionField);
            fieldsPanel.add(createStyledLabel("Option 1:"));
            fieldsPanel.add(option1Field);
            fieldsPanel.add(createStyledLabel("Option 2:"));
            fieldsPanel.add(option2Field);
            fieldsPanel.add(createStyledLabel("Option 3 (optional):"));
            fieldsPanel.add(option3Field);
            fieldsPanel.add(createStyledLabel("Option 4 (optional):"));
            fieldsPanel.add(option4Field);
            fieldsPanel.add(createStyledLabel("Correct Answer:"));
            fieldsPanel.add(correctBox);
            
            p.add(titleLabel, BorderLayout.NORTH);
            p.add(fieldsPanel, BorderLayout.CENTER);
            
            Object[] options = {"Add", "Cancel"};
            
            int result = showStyledOptionDialog(this, p, "Add Question", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                    null, options, options[0]);
            
            if (result != 0) {
                continueAdding = false;
                continue;
            }
            
            String question = questionField.getText().trim();
            String opt1 = option1Field.getText().trim();
            String opt2 = option2Field.getText().trim();
            String opt3 = option3Field.getText().trim();
            String opt4 = option4Field.getText().trim();
            int correct = correctBox.getSelectedIndex() + 1;
            
            if (question.isEmpty() || opt1.isEmpty() || opt2.isEmpty()) {
                showStyledMessageDialog(this, 
                        "Question, Option 1, and Option 2 are required.", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            try {
                try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = c.prepareStatement(
                         """
                         INSERT INTO questions 
                           (question_text, answer_option_1, answer_option_2, 
                            answer_option_3, answer_option_4, correct_answer, assignment_id)
                         VALUES (?, ?, ?, ?, ?, ?, ?)
                         """)) {
                    ps.setString(1, question);
                    ps.setString(2, opt1);
                    ps.setString(3, opt2);
                    ps.setString(4, opt3.isEmpty() ? null : opt3);
                    ps.setString(5, opt4.isEmpty() ? null : opt4);
                    ps.setInt(6, correct);
                    ps.setInt(7, assignmentId);
                    ps.executeUpdate();
                    questionCount++;
                    
                    if (questionCount >= 5) {
                        showStyledMessageDialog(this, 
                                "You've reached the maximum number of questions (5) for this assignment.",
                                "Maximum Reached", JOptionPane.INFORMATION_MESSAGE);
                        continueAdding = false;
                    } else {
                        continueAdding = showStyledConfirmDialog(this, 
                                "Question added successfully. Add another question?", 
                                "Add Another", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                    }
                }
            } catch (Exception ex) {
                showStyledMessageDialog(this, "Error adding question:\n" + ex.getMessage(), 
                        "DB Error", JOptionPane.ERROR_MESSAGE);
                continueAdding = false;
            }
        }
    }

    /* =========================================================
       SYSTEM‑SEARCH
       ========================================================= */
    private void SystemSearch_ButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(DARK_BG);
        
        JLabel titleLabel = new JLabel("System Search", JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEAL_ACCENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        fieldsPanel.setBackground(DARK_BG);
        
        JTextField termField = createStyledTextField();
        
        JComboBox<String> entityBox = new JComboBox<>(
                new String[]{"Students","Instructors","Courses","Classes"});
        entityBox.setFont(REGULAR_FONT);
        entityBox.setBackground(new Color(70, 70, 70));
        entityBox.setForeground(TEXT_LIGHT);
        
        JCheckBox showAllCheck = new JCheckBox("Show All Records");
        showAllCheck.setFont(REGULAR_FONT);
        showAllCheck.setBackground(DARK_BG);
        showAllCheck.setForeground(TEXT_LIGHT);
        
        fieldsPanel.add(createStyledLabel("Search By ID:"));
        fieldsPanel.add(termField);
        fieldsPanel.add(createStyledLabel("Entity:"));
        fieldsPanel.add(entityBox);
        fieldsPanel.add(new JLabel(""));
        fieldsPanel.add(showAllCheck);
        
        p.add(titleLabel, BorderLayout.NORTH);
        p.add(fieldsPanel, BorderLayout.CENTER);
        
        Object[] options = {"Search", "Cancel"};
        
        int result = showStyledOptionDialog(this, p, "System Search", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                null, options, options[0]);
        
        if (result != 0) return; // User clicked Cancel
    
        boolean showAll = showAllCheck.isSelected();
        String term = termField.getText().trim();
        
        if(!showAll && term.isEmpty()){
            showStyledMessageDialog(this, "Enter something to search or check 'Show All'", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return; 
        }
    
        String entity = (String)entityBox.getSelectedItem();
        String sql;
        
        if(showAll) {
            switch(entity) {
                case "Students" -> sql = """
                    SELECT Std_ID AS ID,
                        CONCAT_WS(' ',First_Name,Last_Name) AS Name,
                        Uni_Email AS Email,
                        classes_id AS Class_ID
                    FROM student""";
                case "Instructors" -> sql = """
                    SELECT Inst_id AS ID,
                        CONCAT_WS(' ',First_Name,Last_Name) AS Name,
                        Uni_Email AS Email,
                        Department AS Dept
                    FROM instructors""";
                case "Courses" -> sql = """
                    SELECT courses_id AS ID,
                        courses_Name AS Name,
                        Credit_HR AS Credit_Hours
                    FROM courses""";
                default -> sql = """
                    SELECT Classes_id AS ID,
                        announcement
                    FROM classes""";
            }
        } else {
            switch(entity){
                case "Students" -> sql = """
                    SELECT Std_ID AS ID,
                        CONCAT_WS(' ',First_Name,Last_Name) AS Name,
                        Uni_Email AS Email,
                        classes_id AS Class_ID
                    FROM student
                    WHERE Std_ID LIKE ? OR First_Name LIKE ? OR Last_Name LIKE ? OR Uni_Email LIKE ?""";
                case "Instructors" -> sql = """
                    SELECT Inst_id AS ID,
                        CONCAT_WS(' ',First_Name,Last_Name) AS Name,
                        Uni_Email AS Email,
                        Department AS Dept
                    FROM instructors
                    WHERE Inst_id LIKE ? OR First_Name LIKE ? OR Last_Name LIKE ? OR Uni_Email LIKE ?""";
                case "Courses" -> sql = """
                    SELECT courses_id AS ID,
                        courses_Name AS Name,
                        Credit_HR AS Credit_Hours
                    FROM courses
                    WHERE courses_id LIKE ? OR courses_Name LIKE ?""";
                default -> sql = """
                    SELECT Classes_id AS ID,
                        announcement
                    FROM classes
                    WHERE Classes_id LIKE ? OR announcement LIKE ?""";
            }
        }
    
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            try(Connection c=DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
                PreparedStatement ps=c.prepareStatement(sql)){
                
                if(!showAll) {
                    String like = "%" + term + "%";
                    ps.setString(1, like);
                    ps.setString(2, like);
                    if (entity.equals("Students") || entity.equals("Instructors")) {
                        ps.setString(3, like);
                        ps.setString(4, like);
                    }
                }
    
                try(ResultSet rs=ps.executeQuery()){
                    JTable tbl = new JTable(buildTableModel(rs));
                    tbl.setAutoCreateRowSorter(true);
                    tbl.setFont(REGULAR_FONT);
                    tbl.setBackground(new Color(70, 70, 70));
                    tbl.setForeground(TEXT_LIGHT);
                    tbl.setGridColor(new Color(90, 90, 90));
                    tbl.getTableHeader().setBackground(PANEL_BG);
                    tbl.getTableHeader().setForeground(TEXT_LIGHT);
                    tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
                    
                    JScrollPane scrollPane = new JScrollPane(tbl);
                    scrollPane.setPreferredSize(new Dimension(600, 400));
                    
                    JPanel resultPanel = new JPanel(new BorderLayout(10, 10));
                    resultPanel.setBackground(DARK_BG);
                    
                    JLabel resultTitle = new JLabel("Results: " + entity + (showAll ? " (All)" : ""), JLabel.CENTER);
                    resultTitle.setFont(HEADER_FONT);
                    resultTitle.setForeground(TEAL_ACCENT);
                    resultTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                    
                    resultPanel.add(resultTitle, BorderLayout.NORTH);
                    resultPanel.add(scrollPane, BorderLayout.CENTER);
                    
                    showStyledOptionDialog(this, resultPanel, "Search Results", 
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
                            null, new Object[]{"Close"}, "Close");
                }
            }
        }catch(Exception ex){ 
            showStyledMessageDialog(this, "Search error:\n"+ex.getMessage(), 
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* helper to convert ResultSet → TableModel */
    private static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData m = rs.getMetaData();
        int cols = m.getColumnCount();
        DefaultTableModel tm = new DefaultTableModel();
        for(int i=1;i<=cols;i++) tm.addColumn(m.getColumnLabel(i));
        while(rs.next()){
            Object[] row=new Object[cols];
            for(int i=1;i<=cols;i++) row[i-1]=rs.getObject(i);
            tm.addRow(row);
        }
        return tm;
    }
    
    /* =========================================================
       Profile Picture Methods
       ========================================================= */
    /**
     * Opens a file chooser dialog to select a profile picture
     */
    private void selectProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Read and resize the image
                BufferedImage originalImage = ImageIO.read(selectedFile);
                if (originalImage != null) {
                    profileImage = resizeImage(originalImage, PROFILE_PIC_SIZE, PROFILE_PIC_SIZE);
                    
                    // Convert to bytes for database storage
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(profileImage, "png", baos);
                    profileImageBytes = baos.toByteArray();
                    
                    // Update the UI
                    profilePicLabel.setIcon(new ImageIcon(profileImage));
                    
                    // Save to database
                    saveProfilePictureToDatabase();
                    
                    // Show confirmation
                    showStyledMessageDialog(this, 
                        "Profile picture updated successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showStyledMessageDialog(this, 
                        "Could not read the selected image file.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                showStyledMessageDialog(this, 
                    "Error reading image file:\n" + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Saves the profile picture to the database for the current user
     */
    private void saveProfilePictureToDatabase() {
        if (profileImageBytes == null || currentUserId == -1 || currentRole.isEmpty()) {
            return;
        }
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql;
                if ("student".equals(currentRole)) {
                    sql = "UPDATE student SET profile_image = ? WHERE Std_ID = ?";
                } else if ("instructor".equals(currentRole)) {
                    sql = "UPDATE instructors SET profile_image = ? WHERE Inst_id = ?";
                } else {
                    return; // Not a role with profile image
                }
                
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setBytes(1, profileImageBytes);
                    ps.setInt(2, currentUserId);
                    ps.executeUpdate();
                }
            }
        } catch (Exception ex) {
            showStyledMessageDialog(this, 
                "Error saving profile picture to database:\n" + ex.getMessage(), 
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Loads the profile picture from the database for the current user
     */
    private void loadProfilePictureFromDatabase() {
        if (currentUserId == -1 || currentRole.isEmpty()) {
            return;
        }
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql;
                if ("student".equals(currentRole)) {
                    sql = "SELECT profile_image FROM student WHERE Std_ID = ?";
                } else if ("instructor".equals(currentRole)) {
                    sql = "SELECT profile_image FROM instructors WHERE Inst_id = ?";
                } else {
                    return; // Not a role with profile image
                }
                
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, currentUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            byte[] imageData = rs.getBytes(1);
                            if (imageData != null && imageData.length > 0) {
                                profileImageBytes = imageData;
                                
                                // Convert bytes to image
                                ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                                BufferedImage img = ImageIO.read(bais);
                                if (img != null) {
                                    profileImage = img;
                                    profilePicLabel.setIcon(new ImageIcon(profileImage));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading profile picture: " + ex.getMessage());
        }
    }
    
    /**
     * Resize an image to the specified dimensions
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        // Create a new buffered image with the desired dimensions
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        
        // Set rendering hints for better quality
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the original image into the new one
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        
        return resizedImage;
    }

    /* =========================================================
       HELPERS to create buttons with error handling for icons
       ========================================================= */
    private JButton makeRoleBtn(String t, String icon, Color bg, Font f, 
                               java.awt.event.ActionListener al) {
        // Create the button first
        JButton b = new JButton(t);
        
        // Try to set the icon separately
        try {
            java.net.URL url = getClass().getResource(icon);
            if (url != null) {
                b.setIcon(new ImageIcon(url));
            }
        } catch (Exception e) {
            // Just continue without the icon
        }
        
        // Set the basic button properties
        b.setBackground(bg);
        b.setFont(f);
        b.setForeground(Color.WHITE);
        b.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Create final references for the hover effect
        final JButton finalB = b;
        final Color finalBg = bg;
        
        // Add hover effect
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                finalB.setBackground(finalBg.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                finalB.setBackground(finalBg);
            }
        });
        
        // Add the action listener
        b.addActionListener(al);
        
        return b;
    }

    private JButton makeAdminBtn(String t, String icon, Color bg, Font f) {
        // Create the button first
        JButton b = new JButton(t);
        
        // Try to set the icon separately
        try {
            java.net.URL url = getClass().getResource(icon);
            if (url != null) {
                b.setIcon(new ImageIcon(url));
            }
        } catch (Exception e) {
            // Just continue without the icon
        }
        
        // Set the basic button properties
        b.setBackground(bg);
        b.setFont(f);
        b.setForeground(Color.WHITE);
        b.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Create final references for the hover effect
        final JButton finalB = b;
        final Color finalBg = bg;
        
        // Add hover effect
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                finalB.setBackground(finalBg.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                finalB.setBackground(finalBg);
            }
        });
        
        return b;
    }

    /* =========================================================
       ROLE BUTTON ACTIONS, TOP‑BAR, AUTH, VISIBILITY
       ========================================================= */
    private void Student_ButtonActionPerformed(java.awt.event.ActionEvent e){
        currentRole="student"; setRoleButtonsVisible(false);
        setAdminButtonsVisible(false); setLoginVisible(true);}
    private void Instructor_ButtonActionPerformed(java.awt.event.ActionEvent e){
        currentRole="instructor"; setRoleButtonsVisible(false);
        setAdminButtonsVisible(false); setLoginVisible(true);}
    private void Admin_ButtonActionPerformed(java.awt.event.ActionEvent e){
        currentRole="admin"; setRoleButtonsVisible(false);
        setAdminButtonsVisible(false); setLoginVisible(true);}

    private void Main_Page_btnActionPerformed(java.awt.event.ActionEvent e){
        setRoleButtonsVisible(true); setAdminButtonsVisible(false);
        instructorDashboardPanel.setVisible(false);
        setLoginVisible(false); choosing_panel.setVisible(true);
        dashboard_panel.setVisible(false); currentRole="";
        currentUserId = -1;
        Username_jTextField1.setText(""); password_jTextField1.setText("");
        
        // Reset profile picture to default
        try {
            BufferedImage defaultIcon = new BufferedImage(PROFILE_PIC_SIZE, PROFILE_PIC_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = defaultIcon.createGraphics();
            g2d.setColor(new Color(150, 150, 150));
            g2d.fillOval(0, 0, PROFILE_PIC_SIZE, PROFILE_PIC_SIZE);
            g2d.setColor(new Color(200, 200, 200));
            g2d.fillOval(PROFILE_PIC_SIZE/4, PROFILE_PIC_SIZE/4, PROFILE_PIC_SIZE/2, PROFILE_PIC_SIZE/2);
            g2d.dispose();
            
            profilePicLabel.setIcon(new ImageIcon(defaultIcon));
        } catch (Exception ex) {
            System.err.println("Error creating default profile icon: " + ex.getMessage());
        }
    }
    
    private void AboutUs_btnActionPerformed(java.awt.event.ActionEvent e){
        // Make About Us panel scrollable
        JPanel aboutPanel = new JPanel(new BorderLayout(0, 15));
        aboutPanel.setBackground(DARK_BG);
        
        JLabel titleLabel = new JLabel("About Us", JLabel.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(TEAL_ACCENT);
        
        JTextArea aboutText = new JTextArea("""
            We are CCSIT students at IAU. Our LMS project streamlines communication
            between instructors, students, and administrators.

            Team:
            - Saleh Alsulmi
            - Ammar Bunajmah
            - Dhiyaa Madkhali
            - Mohammed Al Ammar
            - Hassan Almomattin
            - Mohammed Almehdar
            - Abdulmohsin M Almarzouq
            """);
        aboutText.setFont(REGULAR_FONT);
        aboutText.setForeground(TEXT_LIGHT);
        aboutText.setBackground(DARK_BG);
        aboutText.setEditable(false);
        aboutText.setLineWrap(true);
        aboutText.setWrapStyleWord(true);
        
        // Create a scroll pane for the text area
        JScrollPane scrollPane = new JScrollPane(aboutText);
        scrollPane.setBackground(DARK_BG);
        scrollPane.setBorder(null);
        
        aboutPanel.add(titleLabel, BorderLayout.NORTH);
        aboutPanel.add(scrollPane, BorderLayout.CENTER);
        
        showStyledOptionDialog(this, aboutPanel, "About Us", 
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, 
                null, new Object[]{"Close"}, "Close");
    }

    private void password_jTextField1ActionPerformed(java.awt.event.ActionEvent e){
        authenticate();
    }

    private void authenticate(){
        String id=Username_jTextField1.getText().trim(),
               pw=password_jTextField1.getText().trim();
        if(currentRole.isEmpty()){
            showStyledMessageDialog(this, "Choose a role first",
                    "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(id.isEmpty()||pw.isEmpty()){
            showStyledMessageDialog(this, "Enter ID and password",
                    "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String t,c;
        switch(currentRole){
            case "admin"->{t="admins";      c="Admins_id";}
            case "instructor"->{t="instructors"; c="Inst_id";}
            case "student"->{t="student";   c="Std_ID";}
            default->{return;}
        }
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            try(Connection cn=DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
                PreparedStatement ps=cn.prepareStatement("SELECT Log_cred FROM "+t+" WHERE "+c+"=?")){
                ps.setInt(1,Integer.parseInt(id));
                try(ResultSet rs=ps.executeQuery()){
                    if(rs.next()&&pw.equals(rs.getString(1))){
                        currentUserId = Integer.parseInt(id);
                        
                        showStyledMessageDialog(this, "Login successful as "+currentRole+"!",
                                "Welcome", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Load profile picture from database
                        loadProfilePictureFromDatabase();
                        
                        if("admin".equals(currentRole)){ setAdminButtonsVisible(true); setLoginVisible(false); }
                        if("student".equals(currentRole)){ 
                            choosing_panel.setVisible(false); 
                            dashboard_panel.setVisible(true);  
                            loadStudentCourses(currentUserId);
                            loadStudentGrades(currentUserId);
                        }
                        if("instructor".equals(currentRole)){
                            choosing_panel.setVisible(false);
                            dashboard_panel.setVisible(false);
                            instructorDashboardPanel.setVisible(true);
                            loadInstructorClasses(currentUserId);
                        }
                    }else {
                        showStyledMessageDialog(this, "Invalid ID or password",
                                "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }catch(Exception ex){
            showStyledMessageDialog(this, "DB error:\n"+ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setRoleButtonsVisible(boolean v){
        Student_Button.setVisible(v); Instructor_Button.setVisible(v); Admin_Button.setVisible(v);}
    private void setAdminButtonsVisible(boolean v){
        AddStudent_Button.setVisible(v); AddInstructor_Button.setVisible(v);
        CreateCourse_Button.setVisible(v); CreateClass_Button.setVisible(v); 
        LinkCourseClass_Button.setVisible(v); LinkInstructorClass_Button.setVisible(v);
        AssignInstructorCourseClass_Button.setVisible(v);
        SystemSearch_Button.setVisible(v);}
    private void setLoginVisible(boolean v){
        Username_jTextField1.setVisible(v); password_jTextField1.setVisible(v);
        Username_label.setVisible(v); Password_label.setVisible(v); Login_label.setVisible(v);}
}