package com.lumiomedical.flow.node;

import java.util.*;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/03/02
 */
public abstract class AbstractNode implements Node
{
    private final String uid;
    final List<Node> downstream;
    final List<Node> requirements;
    final List<Node> requiredBy;

    /**
     *
     */
    public AbstractNode()
    {
        this.uid = UUID.randomUUID().toString();
        this.downstream = new ArrayList<>();
        this.requirements = new ArrayList<>();
        this.requiredBy = new ArrayList<>();
    }

    @Override
    public String getUid()
    {
        return this.uid;
    }

    @Override
    public List<Node> getDownstream()
    {
        return this.downstream;
    }

    @Override
    public List<Node> getRequirements()
    {
        return this.requirements;
    }

    @Override
    public List<Node> getRequiredBy()
    {
        return this.requiredBy;
    }

    @Override
    public Node after(Node other)
    {
        if (other.getRequirements().contains(this))
            throw new RuntimeException(
                "You are attempting an illegal requirement towards " + other.getClass() + "#" + other.getUid()
                    + " as it already declared itself dependent upon this (" + this.getClass() + "#" + this.getUid() + ")"
            );
        this.requirements.add(other);
        other.getRequiredBy().add(this);
        return this;
    }

    @Override
    public Node after(Collection<Node> others)
    {
        others.forEach(this::after);
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AbstractNode that = (AbstractNode) o;
        return this.uid.equals(that.uid);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.uid);
    }
}
