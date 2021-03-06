package nybsys.tillboxweb.entities;
import nybsys.tillboxweb.BaseEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
public class ProductType extends BaseEntity {

    @Id
    @GeneratedValue(generator = "IdGen")
    @GenericGenerator(name = "IdGen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters =
                    {
                            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "productTypeID_seq"),
                            @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                            @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                            @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    })
    private Integer productTypeID;

    @Column
    @NotNull(message = "Country Code cannot be empty")
    private String name;

    public Integer getProductTypeID() {
        return productTypeID;
    }

    public void setProductTypeID(Integer productTypeID) {
        this.productTypeID = productTypeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductType)) return false;
        if (!super.equals(o)) return false;
        ProductType that = (ProductType) o;
        return Objects.equals(getProductTypeID(), that.getProductTypeID()) &&
                Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getProductTypeID(), getName());
    }


    @Override
    public String toString() {
        return "ProductType{" +
                "productTypeID=" + productTypeID +
                ", name='" + name + '\'' +
                '}';
    }
}
