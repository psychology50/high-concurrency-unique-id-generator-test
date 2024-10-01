package kr.co.idgenerator.strategy;

import com.github.f4b6a3.ulid.UlidCreator;

public class UlidMonotonicGenerator implements IdGenerator<String> {
    @Override
    public String execute() {
        return UlidCreator.getMonotonicUlid().toString();
    }
}
