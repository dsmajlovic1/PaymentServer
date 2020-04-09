package ba.unsa.etf.si.payment.controller;

import ba.unsa.etf.si.payment.model.ApplicationUser;
import ba.unsa.etf.si.payment.model.BankAccount;
import ba.unsa.etf.si.payment.model.BankAccountUser;
import ba.unsa.etf.si.payment.model.MoneyTransfer;
import ba.unsa.etf.si.payment.request.Transfer.MoneyTransferInRequest;
import ba.unsa.etf.si.payment.request.Transfer.MoneyTransferRequest;
import ba.unsa.etf.si.payment.response.Transfer.MoneyTransferResponse;
import ba.unsa.etf.si.payment.response.Transfer.TransferResponse;
import ba.unsa.etf.si.payment.security.CurrentUser;
import ba.unsa.etf.si.payment.security.UserPrincipal;
import ba.unsa.etf.si.payment.service.ApplicationUserService;
import ba.unsa.etf.si.payment.service.BankAccountService;
import ba.unsa.etf.si.payment.service.BankAccountUserService;
import ba.unsa.etf.si.payment.service.MoneyTransferService;
import ba.unsa.etf.si.payment.util.MoneyTransferStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/accounts/moneyTransfer")
public class MoneyTransferController {
    private final MoneyTransferService moneyTransferService;
    private final BankAccountUserService bankAccountUserService;
    private final ApplicationUserService applicationUserService;
    private final BankAccountService bankAccountService;

    public MoneyTransferController(MoneyTransferService moneyTransferService, BankAccountUserService bankAccountUserService, ApplicationUserService applicationUserService, BankAccountService bankAccountService) {
        this.moneyTransferService = moneyTransferService;
        this.bankAccountUserService = bankAccountUserService;
        this.applicationUserService = applicationUserService;
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/allReceives/{bankAccountUserId}")
    public MoneyTransferResponse getAllReceives(@PathVariable Long bankAccountUserId, @CurrentUser UserPrincipal currentUser){
        if(!bankAccountUserService.existsByIdAndUserId(bankAccountUserId,currentUser.getId()))
            return new MoneyTransferResponse(MoneyTransferStatus.CANCELED,"This account does not belong to this user!",null);
        else{
            List<TransferResponse> moneyTransfers;
            BankAccountUser bankAccountUser = bankAccountUserService.findBankAccountUserById(bankAccountUserId);
            moneyTransfers = moneyTransferService.findAllReceives(bankAccountUser.getBankAccount());
            return new MoneyTransferResponse(MoneyTransferStatus.OK,"All payments to this account", moneyTransfers);
        }
    }
    @GetMapping("/allSends/{bankAccountUserId}")
    public MoneyTransferResponse getAllSends(@PathVariable Long bankAccountUserId, @CurrentUser UserPrincipal currentUser){
        if(!bankAccountUserService.existsByIdAndUserId(bankAccountUserId,currentUser.getId()))
            return new MoneyTransferResponse(MoneyTransferStatus.CANCELED,"This account does not belong to this user!",null);
        else{
            List<TransferResponse> moneyTransfers;
            BankAccountUser bankAccountUser = bankAccountUserService.findBankAccountUserById(bankAccountUserId);
            moneyTransfers = moneyTransferService.findAllSends(bankAccountUser.getBankAccount());
            return new MoneyTransferResponse(MoneyTransferStatus.OK,"All payments from this account", moneyTransfers);
        }
    }
    @PostMapping("/outerTransfer")
    public MoneyTransferResponse makeOuterTransfer(@Valid @RequestBody MoneyTransferRequest moneyTransferRequest,
                                                   @CurrentUser UserPrincipal userPrincipal) {
        //todo validacije
        //Onaj ko salje zahtjev on daje novac, pa je on source
        ApplicationUser user = applicationUserService.find(userPrincipal.getId());

        if (!moneyTransferRequest.getAnswer().equals(user.getAnswer().getText())) {
            return new MoneyTransferResponse(MoneyTransferStatus.CANCELED, "Unauthorized request",null);

        }

            /*

            BankAccountUser bankAccountUserSource = bankAccountUserService
                    .findBankAccountUserByIdAndApplicationUserId(moneyTransferRequest.getSends(),
                            userPrincipal.getId());
            BankAccountUser bankAccountUserDest = bankAccountUserService
                    .findBankAccountUserByIdAndApplicationUserId(moneyTransferRequest.getReceives(),
                            moneyTransferRequest.getDestAccountOwnerId());
            if (bankAccountUserSource == null)
                return new TransferDataResponse(MoneyTransferStatus.CANCELED, "Bank account intended to be the source of funds does not belong " +
                        "to this user!");

            if (bankAccountUserDest == null)
                return new TransferDataResponse(MoneyTransferStatus.CANCELED, "Bank account intended to be the destination of funds does not belong " +
                        "to the user whose id was provided!");
            BankAccount source = bankAccountUserSource.getBankAccount();
            BankAccount dest = bankAccountUserDest.getBankAccount();
            if (source.getBalance() < moneyTransferRequest.getAmount())
                return new TransferDataResponse(MoneyTransferStatus.CANCELED, "Not enough funds to proceed with transfer!");

            dest.setBalance(dest.getBalance() + moneyTransferRequest.getAmount());
            source.setBalance(source.getBalance() - moneyTransferRequest.getAmount());
            bankAccountService.save(dest);
            bankAccountService.save(source);

            //todo save
            //dodati historiju transfera
            MoneyTransfer moneyTransfer = new MoneyTransfer(source, dest, moneyTransferRequest.getAmount());
            moneyTransferService.save(moneyTransfer);

            return new TransferDataResponse(MoneyTransferStatus.OK, dest.getCardNumber(), source.getCardNumber(), new Date(),
                    "Successfully transfered funds!");*/
        return processTheTransfer(moneyTransferRequest, userPrincipal.getId());

    }

    @PostMapping("/innerTransfer")
    public MoneyTransferResponse makeInnerTransfer(@Valid @RequestBody MoneyTransferInRequest moneyTransferInRequest,
                                                   @CurrentUser UserPrincipal userPrincipal) {

        //najlakse bilo napraviti request pri cemu je id vlasnika oba racuna isti
        //to je jedina razlika u odnosu na outer
        MoneyTransferRequest moneyTransferRequest = new MoneyTransferRequest(userPrincipal.getId(),moneyTransferInRequest.getSourceBankAccount(),
                moneyTransferInRequest.getDestinationBankAccount(), moneyTransferInRequest.getAmount());
        return processTheTransfer(moneyTransferRequest, userPrincipal.getId());


    }

    private MoneyTransferResponse processTheTransfer(MoneyTransferRequest moneyTransferRequest, Long currentUserId) {
        BankAccountUser bankAccountUserSource = bankAccountUserService
                .findBankAccountUserByIdAndApplicationUserId(moneyTransferRequest.getSourceBankAccount(),
                        currentUserId);
        BankAccountUser bankAccountUserDest = bankAccountUserService
                .findBankAccountUserByIdAndApplicationUserId(moneyTransferRequest.getDestinationBankAccount(),
                        moneyTransferRequest.getDestAccountOwnerId());
        if (bankAccountUserSource == null)
            return new MoneyTransferResponse(MoneyTransferStatus.CANCELED, "Bank account intended to be the source of funds does not belong " +
                    "to this user!",null);

        if (bankAccountUserDest == null)
            return new MoneyTransferResponse(MoneyTransferStatus.CANCELED, "Bank account intended to be the destination of funds does not belong " +
                    "to the user whose id was provided!",null);
        BankAccount source = bankAccountUserSource.getBankAccount();
        BankAccount dest = bankAccountUserDest.getBankAccount();
        if (source.getBalance() < moneyTransferRequest.getAmount())
            return new MoneyTransferResponse(MoneyTransferStatus.CANCELED, "Not enough funds to proceed with transfer!", null);

        dest.setBalance(dest.getBalance() + moneyTransferRequest.getAmount());
        source.setBalance(source.getBalance() - moneyTransferRequest.getAmount());
        bankAccountService.save(dest);
        bankAccountService.save(source);

        //todo save
        //dodati historiju transfera
        MoneyTransfer moneyTransfer = new MoneyTransfer(source, dest, moneyTransferRequest.getAmount());
        MoneyTransfer moneyTransfer1= moneyTransferService.save(moneyTransfer);
        TransferResponse transferResponse=new TransferResponse(moneyTransfer1.getId(), dest.getCardNumber(),
                source.getCardNumber(), moneyTransfer1.getCreatedAt(),moneyTransferRequest.getAmount());

        return new MoneyTransferResponse(MoneyTransferStatus.OK,
                "Successfully transfered funds!", Collections.singletonList(transferResponse));

    }
}
