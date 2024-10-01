package kr.co.idgenerator.strategy;

import com.github.f4b6a3.tsid.TsidCreator;

public class TsidLongGenerator implements IdGenerator<Long> {
    @Override
    public Long execute() {
        return TsidCreator.getTsid().toLong();
    }
}
