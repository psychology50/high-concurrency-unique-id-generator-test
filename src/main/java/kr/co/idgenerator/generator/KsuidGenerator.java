package kr.co.idgenerator.generator;

import com.github.f4b6a3.ksuid.KsuidCreator;

public class KsuidGenerator implements IdGenerator<String> {

    @Override
    public String execute() {
        return KsuidCreator.getKsuid().toString();
    }
}
