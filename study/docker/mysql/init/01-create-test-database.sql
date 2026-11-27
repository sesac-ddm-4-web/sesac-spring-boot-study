-- 테스트가 개발 데이터를 지우지 않도록 DB를 분리한다.
CREATE DATABASE IF NOT EXISTS study_test;
GRANT ALL PRIVILEGES ON study_test.* TO 'study'@'%';
