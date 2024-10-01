package kr.co.idgenerator.generator;

import java.util.UUID;

public class UuidGenerator implements IdGenerator<String> {
    @Override
    public String execute() {
        return UUID.randomUUID().toString();
    }
}
