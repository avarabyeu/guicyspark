DROP TABLE validation IF EXISTS;
CREATE TABLE validation (
  id INTEGER IDENTITY,
  date datetime DEFAULT NULL,
  error varchar(255),
  url varchar(255) DEFAULT NULL,
  status varchar(255) DEFAULT NULL);

-- ----------------------------
-- Records of validation
-- ----------------------------
INSERT INTO validation VALUES ('1', '2014-10-14 17:31:10', NULL, 'https://google.com', 'OK');
INSERT INTO validation VALUES ('2', '2014-10-14 17:31:30', NULL, 'http://www.apple.com', 'OK');
