CREATE DATABASE project2;
USE project2;

-- Create admins table (just for storing admin information)
CREATE TABLE admins (
    Admins_id INT PRIMARY KEY,
    First_Name VARCHAR(45) NOT NULL,
    Last_Name VARCHAR(45) NOT NULL,
    Gender VARCHAR(45) NOT NULL,
    Log_cred VARCHAR(45) NOT NULL
);

-- Create classes table
CREATE TABLE classes (
	Class_name VARCHAR(45),
    Classes_id INT AUTO_INCREMENT PRIMARY KEY,
    announcement VARCHAR(1200)
);

-- Create courses table (each course can be taught in multiple classes)
CREATE TABLE courses (
    courses_id INT AUTO_INCREMENT PRIMARY KEY,
    courses_Name VARCHAR(45) NOT NULL,
    Credit_HR INT NOT NULL
);

-- Create a many-to-many relationship table between courses and classes
CREATE TABLE course_class (
    course_id INT,
    class_id INT,
    PRIMARY KEY (course_id, class_id),
    FOREIGN KEY (course_id) REFERENCES courses(courses_id),
    FOREIGN KEY (class_id) REFERENCES classes(Classes_id)
);

-- Create instructors table (each instructor teaches multiple courses)
CREATE TABLE instructors (
    Inst_id INT PRIMARY KEY,
    First_Name VARCHAR(45) NOT NULL,
    Last_Name VARCHAR(45) NOT NULL,
    Gender VARCHAR(45) NOT NULL,
    Uni_Email VARCHAR(45) NOT NULL,
    Log_cred VARCHAR(45) NOT NULL,
    profile_image MEDIUMBLOB,
    Department VARCHAR(45) NOT NULL
);

-- Create students table (each student belongs to one class, and a class has multiple students)
CREATE TABLE student (
    Std_ID INT PRIMARY KEY,
    First_Name VARCHAR(45) NOT NULL,
    Last_Name VARCHAR(45) NOT NULL,
    Gender VARCHAR(45) NOT NULL,
    Uni_Email VARCHAR(45) NOT NULL,
    Log_cred VARCHAR(45) NOT NULL,
    profile_image MEDIUMBLOB,
    classes_id INT,  -- Foreign key from classes table
    FOREIGN KEY (classes_id) REFERENCES classes(Classes_id)
);

-- Create quizzes table
CREATE TABLE quizzes (
    Quiz_id INT AUTO_INCREMENT PRIMARY KEY,
    Quiz_Name VARCHAR(100) NOT NULL,
    Quiz_Grade DECIMAL(5, 2),
    classes_id INT,  -- Foreign key from classes table
    FOREIGN KEY (classes_id) REFERENCES classes(Classes_id)
);

-- Create assignments table
CREATE TABLE assignments (
    Assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    Assignment_Name VARCHAR(100) NOT NULL,
    Assignment_Grade DECIMAL(5, 2),
    classes_id INT,  -- Foreign key from classes table
    FOREIGN KEY (classes_id) REFERENCES classes(Classes_id)
);

-- Create questions table
CREATE TABLE questions (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    question_text VARCHAR(255) NOT NULL,
    answer_option_1 VARCHAR(255),
    answer_option_2 VARCHAR(255),
    answer_option_3 VARCHAR(255),
    answer_option_4 VARCHAR(255),
    correct_answer INT,  -- This could store the index of the correct answer (1-4)
    quiz_id INT,         -- Foreign key from quizzes table
    assignment_id INT,   -- Foreign key from assignments table
    FOREIGN KEY (quiz_id) REFERENCES quizzes(Quiz_id),
    FOREIGN KEY (assignment_id) REFERENCES assignments(Assignment_id)
);

-- Create a many-to-many relationship table between instructors and classes
CREATE TABLE instructor_class (
    instructor_id INT,
    class_id INT,
    PRIMARY KEY (instructor_id, class_id),
    FOREIGN KEY (instructor_id) REFERENCES instructors(Inst_id),
    FOREIGN KEY (class_id) REFERENCES classes(Classes_id)
);

-- Trigger to check the number of questions in a quiz (limit to 3 questions)
DELIMITER $$

CREATE TRIGGER check_quiz_question_limit
BEFORE INSERT ON questions
FOR EACH ROW
BEGIN
    DECLARE quiz_question_count INT;
    SELECT COUNT(*) INTO quiz_question_count
    FROM questions
    WHERE quiz_id = NEW.quiz_id;
    
    IF quiz_question_count >= 3 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot add more than 3 questions to a quiz';
    END IF;
END $$

DELIMITER ;

-- Trigger to check the number of questions in an assignment (limit to 5 questions)
DELIMITER $$

CREATE TRIGGER check_assignment_question_limit
BEFORE INSERT ON questions
FOR EACH ROW
BEGIN
    DECLARE assignment_question_count INT;
    SELECT COUNT(*) INTO assignment_question_count
    FROM questions
    WHERE assignment_id = NEW.assignment_id;
    
    IF assignment_question_count >= 5 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot add more than 5 questions to an assignment';
    END IF;
END $$

DELIMITER ;

-- Create student_quiz_grades table to track individual student grades for quizzes
CREATE TABLE student_quiz_grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    quiz_id INT NOT NULL,
    score DECIMAL(5, 2) NOT NULL,
    submission_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    feedback VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES student(Std_ID),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(Quiz_id),
    -- Ensure each student has only one grade entry per quiz
    UNIQUE KEY unique_student_quiz (student_id, quiz_id)
);

-- Create student_assignment_grades table to track individual student grades for assignments
CREATE TABLE student_assignment_grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    assignment_id INT NOT NULL,
    score DECIMAL(5, 2) NOT NULL,
    submission_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    feedback VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES student(Std_ID),
    FOREIGN KEY (assignment_id) REFERENCES assignments(Assignment_id),
    -- Ensure each student has only one grade entry per assignment
    UNIQUE KEY unique_student_assignment (student_id, assignment_id)
);
select * from classes;
select * from instructors;
insert into admins
values(1, "Ammar", "Bunajmah", "M", "admin");

-- Class 1 (2023)
INSERT INTO student (Std_ID, First_Name, Last_Name, Gender, Uni_Email, Log_cred, profile_image, classes_id)
VALUES
(2301, 'Abdulmohsen', 'Almarzoq', 'M', '2301@prup.edu.sa', '2301', NULL, 1),
(2302, 'Mohammed', 'Alshuhri', 'M', '2302@prup.edu.sa', '2302', NULL, 1),
(2303, 'Ali', 'Alzidan', 'M', '2303@prup.edu.sa', '2303', NULL, 1),
(2304, 'Salem', 'Balkhair', 'M', '2304@prup.edu.sa', '2304', NULL, 1),
(2305, 'Manaf', 'Alahmed', 'M', '2305@prup.edu.sa', '2305', NULL, 1);

-- Class 2 (2022)
INSERT INTO student (Std_ID, First_Name, Last_Name, Gender, Uni_Email, Log_cred, profile_image, classes_id)
VALUES
(2201, 'Dhyia', 'Madkhli', 'M', '2201@prup.edu.sa', '2201', NULL, 2),
(2202, 'Saleh', 'Alsulmi', 'M', '2202@prup.edu.sa', '2202', NULL, 2),
(2203, 'Mohammed', 'Almehdar', 'M', '2203@prup.edu.sa', '2203', NULL, 2),
(2204, 'Hassan', 'Almomatn', 'M', '2204@prup.edu.sa', '2204', NULL, 2),
(2205, 'Ammar', 'Bunajmah', 'M', '2205@prup.edu.sa', '2205', NULL, 2);

