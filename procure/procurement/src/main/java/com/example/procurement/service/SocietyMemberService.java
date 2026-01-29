package com.example.procurement.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.procurement.DTO.SocietyMemberRequest;
import com.example.procurement.config.PasswordGenerator;
import com.example.procurement.entity.AppUser;
import com.example.procurement.entity.Group;
import com.example.procurement.entity.Society;
import com.example.procurement.entity.SocietyUser;
import com.example.procurement.repository.GroupsRepo;
import com.example.procurement.repository.SocietyRepo;
import com.example.procurement.repository.SocietyUserRepository;
import com.example.procurement.repository.UserLoginRepo;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocietyMemberService {

      @Autowired
   PasswordGenerator passwordGenerator;

        @Autowired
   PasswordEncoder passwordEncoder;
     @Autowired
   GroupsRepo groupRepo;
        @Autowired
   UserLoginRepo userLoginRepo;
    private final SocietyUserRepository repo;
     private final SocietyRepo societyrepo;

    public SocietyUser onboard(SocietyMemberRequest dto) {

        Optional<Society> society=societyrepo.findById(dto.getSocietyId().intValue());
        SocietyUser member = new SocietyUser();
      member.setFullName(dto.getFirstName() + " " + dto.getLastName());
    member.setEmail(dto.getEmail());
    member.setPhone(dto.getPhone());

    member.setBlock(dto.getBlock());
    member.setFlatNumber(dto.getFlatNumber());

    // Owner OR Tenant
    member.setOwner("OWNER".equalsIgnoreCase(dto.getOwnershipType()));

    member.setRole(dto.getRole());
    member.setActive(true);
    member.setJoinedOn(LocalDate.now());
    member.setSociety(society.get());
     SocietyUser savedUser   = repo.save(member);

         // 2️⃣ Generate secure password
    String securePassword = passwordGenerator.createSecurePassword();
    if (securePassword == null || securePassword.isEmpty()) {
        throw new IllegalStateException("Failed to generate secure password");
    }

    // 3️⃣ Create User Login
    AppUser newVendor = new AppUser();
    newVendor.setEmail(savedUser.getEmail());
    newVendor.setIdempuser(savedUser);
    newVendor.setLoginType("Society");
    //newVendor.setTypePassword("Temporary");
    newVendor.setUsername(savedUser.getEmail());
    newVendor.setEnabled(true);
    newVendor.setPassword(passwordEncoder.encode(securePassword));
    newVendor.setDt_enable(LocalDate.now());
System.out.println(securePassword);
    // 4️⃣ Assign Group
    Group grp = groupRepo.findFirstByName("SOCIETY_MEMBER")
            .orElseThrow(() -> new EntityNotFoundException("Supplier group not found"));
    newVendor.setIdGroup(grp);

    // 5️⃣ Save Login
    userLoginRepo.save(newVendor);
return savedUser;
    }

    public List<SocietyUser> findAll() {
        return repo.findAll();
    }

   
}

