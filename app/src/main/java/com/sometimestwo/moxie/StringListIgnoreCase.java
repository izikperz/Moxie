package com.sometimestwo.moxie;

import java.util.ArrayList;

public class StringListIgnoreCase extends ArrayList<String> {

    @Override
    public boolean contains(Object o) {
        String paramStr = (String)o;
        for (String s : this) {
            if (paramStr.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        String paramStr = (String)o;
        for (String s : this) {
            if (paramStr.equalsIgnoreCase(s)) {
                super.remove(s);
                return true;
            }
        }
        return false;
    }
}
