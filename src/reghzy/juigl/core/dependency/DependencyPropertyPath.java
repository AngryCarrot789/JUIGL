package reghzy.juigl.core.dependency;

import reghzy.juigl.core.utils.Size;

import java.util.Objects;

public final class DependencyPropertyPath {
    String name;
    Class<?> ownerType;

    public DependencyPropertyPath(String name, Class<?> ownerType) {
        this.name = name;
        this.ownerType = ownerType;
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getOwnerType() {
        return this.ownerType;
    }

    @Override
    public final String toString() {
        return this.ownerType.getName() + "::" + this.name;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof DependencyPropertyPath))
            return false;
        DependencyPropertyPath other = (DependencyPropertyPath) o;
        return Objects.equals(this.name, other.name) && Objects.equals(this.ownerType, other.ownerType);
    }

    @Override
    public final int hashCode() {
        int result = this.name != null ? this.name.hashCode() : 0;
        result = 31 * result + (this.ownerType != null ? this.ownerType.hashCode() : 0);
        return result;
    }
}
