package me.timwastaken.speedmission;
import java.util.Map;

final class Pair<K, V> implements Map.Entry<K, V> {

    private K key;
    private V value;

    public Pair() {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    public K setKey(K key) {
        K old = this.key;
        this.key = key;
        return old;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }

}