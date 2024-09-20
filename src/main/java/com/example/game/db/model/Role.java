package com.example.game.db.model;

import com.example.game.db.enums.RoleType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role extends BaseEntity {

	@Enumerated(STRING)
	@NotNull
	private RoleType name;

	@NotBlank
	private String label;

	@NotBlank
	private String description;

	@ManyToMany(mappedBy = "roles")
	private Collection<User> users;
}