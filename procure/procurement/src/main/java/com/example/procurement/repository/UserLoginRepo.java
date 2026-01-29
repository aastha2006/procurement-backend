package com.example.procurement.repository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.procurement.entity.AppUser;

public interface UserLoginRepo extends JpaRepository<AppUser,Integer>
{

   // List<UserLogin> findAllByUpermissionUsertypeIdusertypeIn(List<Integer> ids);

  // UserLogin findByNmloginAndPwdAndEnabled(String userName, String passwordString, String string);


    
    AppUser findByUsername(String username);

   

     AppUser findByEmail(String email);

     AppUser findByResetToken(String token);

     @Query("Select u From AppUser u where u.email =:email ")
     AppUser findUserByIdemail(@Param("email") String email);

     
     Optional<AppUser> findByIdempuserId(Integer id);
 
     @Query("SELECT u FROM AppUser u WHERE u.dt_enable = :today OR u.dt_disable = :today")
List<AppUser> findUsersWithEnableOrDisableDate(@Param("today") LocalDate today);



     AppUser findByUsernameAndLoginType(String nmLogin, String loginType);

}
