package ba.unsa.etf.si.payment.controller;


import ba.unsa.etf.si.payment.exception.ResourceNotFoundException;
import ba.unsa.etf.si.payment.model.ApplicationUser;
import ba.unsa.etf.si.payment.model.BankAccount;
import ba.unsa.etf.si.payment.model.BankAccountUser;
import ba.unsa.etf.si.payment.response.AccountResponse;
import ba.unsa.etf.si.payment.response.BankAccountDataResponse;
import ba.unsa.etf.si.payment.response.DeleteAccountResponse;
import ba.unsa.etf.si.payment.security.CurrentUser;
import ba.unsa.etf.si.payment.security.UserPrincipal;
import ba.unsa.etf.si.payment.service.BankAccountService;
import ba.unsa.etf.si.payment.service.BankAccountUserService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {
    private final BankAccountUserService bankAccountUserService;
    private final BankAccountService bankAccountService;


    public BankAccountController(BankAccountUserService bankAccountUserService, BankAccountService bankAccountService) {
        this.bankAccountUserService = bankAccountUserService;
        this.bankAccountService = bankAccountService;
    }

    //All accounts that belong to current user
    @GetMapping("/all")
    public List<BankAccountDataResponse> getBankAccounts(@CurrentUser UserPrincipal currentUser){
        return bankAccountUserService.findBankAccounts(currentUser.getId());
    }

    //Add account
    @PostMapping("/add")
    public AccountResponse addBankAccount(@Valid @RequestBody BankAccount bankAccount, @CurrentUser UserPrincipal currentUser) {
        List<BankAccount> bankAccounts=bankAccountService.find(bankAccount.getCvc(), bankAccount.getCardNumber());
        if(bankAccounts.isEmpty()){
            throw new ResourceNotFoundException("Bank account not valid");
        }

        //Uzmemo BankAccount ac=bankAccounts.get(0);
        //uzmemo id
        //i onda provjerimo ima li u banaccountusser vec neki red sa tim idem bankaccount-a
        //ako ima baciti izuzetak
        //todo check if the bank account is already in use

        //todo Here we should add validation of cvc and cardNumer
        //Add validation class in validation package
        //and validate it using java regex
        //If the test fails return new Account Response(false, "Wrong card or cvc format");

        BankAccountUser bankAccountUser=new BankAccountUser();
        ApplicationUser user=new ApplicationUser();
        user.setId(currentUser.getId());
        bankAccountUser.setBankAccount(bankAccounts.get(0));
        bankAccountUser.setApplicationUser(user);
        bankAccountUserService.save(bankAccountUser);
        return new AccountResponse(true, "Succefully added account");
    }
    @DeleteMapping("/delete/{accountId}")
    public DeleteAccountResponse deleteBankAccounts(@PathVariable Long accountId,
                                   @CurrentUser UserPrincipal currentUser){

        if(!bankAccountUserService.existsByIdAndUserId(accountId,currentUser.getId())){
            return new DeleteAccountResponse(false, "Account does not exist!");
        }
        bankAccountUserService.delete(accountId);
        return new DeleteAccountResponse(true, "Successful deletion!");
    }


}