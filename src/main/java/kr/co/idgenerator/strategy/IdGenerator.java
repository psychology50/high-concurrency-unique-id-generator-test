package kr.co.idgenerator.strategy;

public interface IdGenerator<T> {
    T execute();
}
