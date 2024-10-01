package kr.co.idgenerator.generator;

import com.github.f4b6a3.tsid.TsidCreator;

public class TsidGenerator implements IdGenerator<String> {
    @Override
    public String execute() {
        return TsidCreator.getTsid().toString();
    }
}
