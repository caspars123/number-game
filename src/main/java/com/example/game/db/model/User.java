package com.example.game.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;

import static jakarta.persistence.FetchType.EAGER;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class User extends BaseEntity {

	@Email
	@NotBlank
	private String email;
	@NotBlank
	private String password;
	@NotBlank
	private String name;

	@Builder.Default
	private boolean active = true;

	@NotNull
	@CreatedDate
	@Column
	private Instant createdAt;

	@NotNull
	@LastModifiedDate
	@Column
	private Instant updatedAt;

	@ManyToMany(fetch = EAGER)
	@JoinTable(
		name = "users_roles",
		joinColumns = @JoinColumn(
			name = "user_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(
			name = "role_id", referencedColumnName = "id"))
	private Collection<Role> roles;

	@Column(name = "balance", nullable = false)
	private BigDecimal balance;
}