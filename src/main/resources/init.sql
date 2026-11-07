SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS temperature_converter CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE temperature_converter;
CREATE TABLE IF NOT EXISTS trips (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE);
CREATE TABLE IF NOT EXISTS measurements (id BIGINT AUTO_INCREMENT PRIMARY KEY, trip_id BIGINT NOT NULL REFERENCES trips(id), distance_km DOUBLE PRECISION NOT NULL CHECK(distance_km >= 0), duration_hours DOUBLE PRECISION NOT NULL CHECK(duration_hours > 0), speed_kmh DOUBLE PRECISION NOT NULL CHECK(speed_kmh >= 0));
CREATE TABLE IF NOT EXISTS unit (id VARCHAR(16) PRIMARY KEY);
CREATE TABLE IF NOT EXISTS unit_translation (
    unit_id VARCHAR(16) REFERENCES unit(id), lang VARCHAR(2), name VARCHAR(80) NOT NULL,
    description VARCHAR(160) NOT NULL, PRIMARY KEY(unit_id, lang)
);
CREATE TABLE IF NOT EXISTS conversion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, input_value DOUBLE PRECISION NOT NULL,
    source VARCHAR(16) NOT NULL REFERENCES unit(id), target VARCHAR(16) NOT NULL REFERENCES unit(id),
    result DOUBLE PRECISION NOT NULL, lang VARCHAR(2) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
INSERT INTO unit (id) VALUES ('celsius'), ('fahrenheit'), ('kelvin'), ('km'), ('hour'), ('kmh') ON DUPLICATE KEY UPDATE id=VALUES(id);
INSERT INTO unit_translation (unit_id,lang,name,description) VALUES
('celsius','en','Celsius','Water freezes at 0 °C'),
('fahrenheit','en','Fahrenheit','Water freezes at 32 °F'),
('kelvin','en','Kelvin','Absolute temperature scale'),
('km','en','kilometre','One thousand metres'),
('hour','en','hour','Sixty minutes'),
('kmh','en','kilometres per hour','Distance travelled in one hour'),
('celsius','fi','Celsiusaste','Vesi jäätyy 0 °C:ssa'),
('fahrenheit','fi','Fahrenheitaste','Vesi jäätyy 32 °F:ssa'),
('kelvin','fi','kelvin','Absoluuttisen lämpötilan yksikkö'),
('km','fi','kilometri','Tuhat metriä'),
('hour','fi','tunti','Kuusikymmentä minuuttia'),
('kmh','fi','kilometriä tunnissa','Tunnissa kuljettu matka'),
('celsius','sv','grad Celsius','Vatten fryser vid 0 °C'),
('fahrenheit','sv','grad Fahrenheit','Vatten fryser vid 32 °F'),
('kelvin','sv','kelvin','Enhet för absolut temperatur'),
('km','sv','kilometer','Tusen meter'),
('hour','sv','timme','Sextio minuter'),
('kmh','sv','kilometer per timme','Sträcka per timme'),
('celsius','ja','摂氏度','水は0°Cで凍ります'),
('fahrenheit','ja','華氏度','水は32°Fで凍ります'),
('kelvin','ja','ケルビン','絶対温度の単位'),
('km','ja','キロメートル','千メートル'),
('hour','ja','時間','六十分'),
('kmh','ja','キロメートル毎時','一時間に進む距離'),
('celsius','ar','درجة مئوية','يتجمد الماء عند صفر مئوي'),
('fahrenheit','ar','درجة فهرنهايت','يتجمد الماء عند ٣٢ درجة فهرنهايت'),
('kelvin','ar','كلفن','وحدة درجة الحرارة المطلقة'),
('km','ar','كيلومتر','ألف متر'),
('hour','ar','ساعة','ستون دقيقة'),
('kmh','ar','كيلومتر في الساعة','المسافة المقطوعة في ساعة')
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);
