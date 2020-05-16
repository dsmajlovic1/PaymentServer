package ba.unsa.etf.si.payment.repository;

import ba.unsa.etf.si.payment.model.BankAccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountUserRepository extends JpaRepository<BankAccountUser, Long> {

    List<BankAccountUser> findAllByApplicationUser_Id(Long id);
    Boolean existsByIdAndApplicationUser_Id(Long id, Long applicationUser_id);
    List<BankAccountUser> findAllByBankAccount_CardNumber(String cardNumber);
    BankAccountUser findByIdAndApplicationUser_Id(Long bankAccountUserId, Long applicationUserId);
    BankAccountUser findBankAccountUserById(Long id);

}