package com.monolithicauthtest.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Client.
 */
@Entity
@Table(name = "client")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Client implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "orgname")
    private String orgname;

    @Column(name = "clientname")
    private String clientname;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "client")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "client" }, allowSetters = true)
    private Set<Gitrep> gitreps = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Client id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrgname() {
        return this.orgname;
    }

    public Client orgname(String orgname) {
        this.setOrgname(orgname);
        return this;
    }

    public void setOrgname(String orgname) {
        this.orgname = orgname;
    }

    public String getClientname() {
        return this.clientname;
    }

    public Client clientname(String clientname) {
        this.setClientname(clientname);
        return this;
    }

    public void setClientname(String clientname) {
        this.clientname = clientname;
    }

    public Set<Gitrep> getGitreps() {
        return this.gitreps;
    }

    public void setGitreps(Set<Gitrep> gitreps) {
        if (this.gitreps != null) {
            this.gitreps.forEach(i -> i.setClient(null));
        }
        if (gitreps != null) {
            gitreps.forEach(i -> i.setClient(this));
        }
        this.gitreps = gitreps;
    }

    public Client gitreps(Set<Gitrep> gitreps) {
        this.setGitreps(gitreps);
        return this;
    }

    public Client addGitrep(Gitrep gitrep) {
        this.gitreps.add(gitrep);
        gitrep.setClient(this);
        return this;
    }

    public Client removeGitrep(Gitrep gitrep) {
        this.gitreps.remove(gitrep);
        gitrep.setClient(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Client)) {
            return false;
        }
        return getId() != null && getId().equals(((Client) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Client{" +
            "id=" + getId() +
            ", orgname='" + getOrgname() + "'" +
            ", clientname='" + getClientname() + "'" +
            "}";
    }
}
