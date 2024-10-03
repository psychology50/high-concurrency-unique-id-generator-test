package kr.co.idgenerator.strategy;

import com.github.f4b6a3.uuid.UuidCreator;

public class TimeOrderedEpochUuidGenerator implements IdGenerator<String> {

    @Override
    public String execute() {
        return UuidCreator.getTimeOrderedEpoch().toString();
    }
}
