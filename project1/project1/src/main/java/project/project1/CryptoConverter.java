package project.project1;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

@Slf4j
@Converter
public class CryptoConverter implements AttributeConverter<String, String> {

    private final StandardPBEStringEncryptor encryptor;

    // 1. (수정) @Value 생성자 대신, 기본 생성자를 사용합니다.
    public CryptoConverter() {
        // 2. Spring의 @Value를 통하지 않고, Jasypt가 읽는 '그' 환경 변수를 직접 읽어옵니다.
        String password = System.getProperty("jasypt.encryptor.password");
        if (password == null) {
            password = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        }

        if (password == null) {
            log.error("Jasypt 마스터 비밀번호를 찾을 수 없습니다. (jasypt.encryptor.password)");
            // 애플리케이션을 시작할 수 없도록 강제로 예외를 발생시킵니다.
            throw new RuntimeException("Jasypt password is not set in environment/properties");
        }
        // 3. (기존 로직 동일) 수동으로 Encryptor를 설정합니다.
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(password);
        encryptor.setAlgorithm("PBEWithMD5AndDES");
    }

    // 엔티티의 필드 값을 DB 컬럼에 저장할 때 호출됩니다. (암호화)
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return encryptor.encrypt(attribute);
    }

    // DB의 컬럼 값을 엔티티 필드에 매핑할 때 호출됩니다. (복호화)
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return encryptor.decrypt(dbData);
    }
}
