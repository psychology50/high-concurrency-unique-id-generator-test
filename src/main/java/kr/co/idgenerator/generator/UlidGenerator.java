package kr.co.idgenerator.generator;

import com.github.f4b6a3.ulid.UlidCreator;

public class UlidGenerator implements IdGenerator<String> {
    @Override
    public String execute() {
        return UlidCreator.getUlid().toString();
    }
}
