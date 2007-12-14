package com.apress.progwt.client.domain;

import java.io.Serializable;

import com.apress.progwt.client.domain.generated.AbstractProcessType;

public class ProcessType extends AbstractProcessType implements
        Serializable, Loadable, Comparable<ProcessType> {

    public ProcessType() {
    }

    public ProcessType(String name) {
        setName(name);
    }

    @Override
    public String toString() {
        return "ProcessType: " + getName() + " " + getId();
    }

    public int compareTo(ProcessType o) {
        if (o != null) {
            return getStatus_order() - o.getStatus_order();
        } else {
            return 1;
        }

    }

}