package com.lovelypets.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "Managers")
@PrimaryKeyJoinColumn(name = "staff_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Manager extends Staff {

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    private List<Appointment> appointments;
}
