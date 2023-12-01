package com.alexbalmus.jpapractice;

import java.util.List;
import java.util.Objects;

import org.hibernate.proxy.HibernateProxy;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class Author
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Book> books;

    @Column
    private String name;

    // Implementing equals and hashCode for JPA & Hibernate entities:
    //    https://jpa-buddy.com/blog/hopefully-the-final-article-about-equals-and-hashcode-for-jpa-entities-with-db-generated-ids/

    //    The (possibly) less reliable implementation that's portable across JPA implementations:

    //    @Override
    //    public boolean equals(Object o)
    //    {
    //        if (this == o)
    //        {
    //            return true;
    //        }
    //
    //        if (o == null || getClass() != o.getClass())
    //        {
    //            return false;
    //        }
    //
    //        Author other = (Author) o;
    //        return id != null && id.equals(other.getId());
    //    }
    //
    //    @Override
    //    public int hashCode()
    //    {
    //        return getClass().hashCode();
    //    }


    //    A more reliable implementation when using Hibernate (according to the link above):

    @Override
    public final boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null)
        {
            return false;
        }

        Class<?> oEffectiveClass = o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();

        Class<?> thisEffectiveClass = this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();

        if (thisEffectiveClass != oEffectiveClass)
        {
            return false;
        }

        Author other = (Author) o;
        return getId() != null && Objects.equals(getId(), other.getId());
    }

    @Override
    public final int hashCode()
    {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }
}
