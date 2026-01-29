package com.example.procurement.entity;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data

public class AppUser implements UserDetails {
    @Id @GeneratedValue private Long id;
    private String username;
    private String password;
    private String fullName;
    private String email;
   

     @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn (name="id_emp_user",referencedColumnName = "id")
  private SocietyUser idempuser;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn (name="id_ven",referencedColumnName = "id")
  private Vendor idVen;

  @Column(name = "login_type")
   private String loginType;

   @ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "idGroup")
private Group idGroup;

   @Column(name = "reset_token",unique = true)
   private String resetToken;

   @Column(name = "token_expiry_time")
   private LocalDateTime tokenExpiryTime;

   @Column(name = "last_password_changed_at")
   private LocalDateTime lastPasswordChangedAt;

   @ElementCollection
   @CollectionTable(name = "user_password_changes", joinColumns = @JoinColumn(name = "user_id"))
   @Column(name = "change_time")
   private List<LocalDateTime> passwordChangeTimestamps = new ArrayList<>();

  private int failedAttempts = 0;

    private boolean accountLocked = false;

    private LocalDateTime lockTime;

 @Column(name = "enabled")
  private  Boolean enabled= false;


  @Column(name = "dt_enable")
  private LocalDate dt_enable;

  @Column(name = "dt_disable")
  private LocalDate dt_disable;
   
@Override
@JsonIgnore
public Collection<? extends GrantedAuthority> getAuthorities() {

    Set<GrantedAuthority> authorities = new HashSet<>();

    if (idGroup == null || idGroup.getRoles() == null) {
        return authorities;
    }

    idGroup.getRoles().forEach(role -> {

        // Optional: keep ROLE based authority
        authorities.add(
                new SimpleGrantedAuthority("ROLE_" + role.getCode())
        );

        if (role.getModulePermissions() == null) {
            return;
        }

        role.getModulePermissions().forEach(permission -> {

            String module = permission.getModule()
                    .toUpperCase()
                    .replace(" ", "_");

            if (Boolean.TRUE.equals(permission.getCanView())) {
                authorities.add(new SimpleGrantedAuthority(module + ":VIEW"));
            }
            if (Boolean.TRUE.equals(permission.getCanCreate())) {
                authorities.add(new SimpleGrantedAuthority(module + ":CREATE"));
            }
            if (Boolean.TRUE.equals(permission.getCanEdit())) {
                authorities.add(new SimpleGrantedAuthority(module + ":EDIT"));
            }
            if (Boolean.TRUE.equals(permission.getCanDelete())) {
                authorities.add(new SimpleGrantedAuthority(module + ":DELETE"));
            }
            if (Boolean.TRUE.equals(permission.getCanApprove())) {
                authorities.add(new SimpleGrantedAuthority(module + ":APPROVE"));
            }
        });
    });

    return authorities;
}






  @Override
    @JsonIgnore
  public String getPassword() {
   return password;
    }

  @Override
  @JsonIgnore
  public String getUsername() {
    return username;
   }
  @JsonIgnore
 public boolean isEnabled() {
    return enabled;
  }


@JsonIgnore
  public String getEmail() {
    return email;
  }



@Override
public boolean isAccountNonExpired() {
  // TODO Auto-generated method stub
  throw new UnsupportedOperationException("Unimplemented method 'isAccountNonExpired'");
}



@Override
public boolean isAccountNonLocked() {
  // TODO Auto-generated method stub
  throw new UnsupportedOperationException("Unimplemented method 'isAccountNonLocked'");
}



@Override
public boolean isCredentialsNonExpired() {
  // TODO Auto-generated method stub
  throw new UnsupportedOperationException("Unimplemented method 'isCredentialsNonExpired'");
}


















}

