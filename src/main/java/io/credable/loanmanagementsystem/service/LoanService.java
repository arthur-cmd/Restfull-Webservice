package io.credable.loanmanagementsystem.service;

import io.credable.loanmanagementsystem.controller.QueryLoan;
import io.credable.loanmanagementsystem.data.dto.LoanRequestDTO;
import io.credable.loanmanagementsystem.data.dto.LoanResponseDTO;
import io.credable.loanmanagementsystem.data.dto.ScoringDTO;
import io.credable.loanmanagementsystem.data.vo.LoanModel;
import io.credable.loanmanagementsystem.data.dao.Repository.LoanRepository;
import io.credable.loanmanagementsystem.data.vo.Loanstatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);
    private LoanRepository Loanrepository;

    private LoanModel loan;

    private QueryLoan queryLoan;

    public LoanService(LoanRepository loanrepository,QueryLoan queryLoan){
        this.Loanrepository = loanrepository;
        this.queryLoan=queryLoan;

    }

    public LoanModel getLoan(String customerNumber){
        return Loanrepository.findByCustomerNumber(customerNumber);
    }


   public LoanModel createLoan(LoanRequestDTO loanRequestDTO){

        LoanModel newloan= new LoanModel();
        newloan.setCustomerNumber(loanRequestDTO.getCustomerNumber());
        newloan.setAmount(loanRequestDTO.getAmount());

       return Loanrepository.save(newloan);

   }




   public  ResponseEntity<LoanResponseDTO> requestLoan(LoanRequestDTO loanrquest)  {

        LoanModel loansave= createLoan(loanrquest);
        ScoringDTO scoringDTO = null;

        try {

            scoringDTO = queryLoan.queryScore(loanrquest.getCustomerNumber());
        }
        catch (Exception e){
            throw  new RuntimeException(e);
        }

       String loanStatus;

       Double limited_amount= scoringDTO.getLimitAmount();


       if(scoringDTO.getLimitAmount() >=loanrquest.getAmount()){
           loanStatus = String.valueOf(Loanstatus.Succesfull);
       }  else if (scoringDTO.getLimitAmount() == null) {  //TODO conditional
           loanStatus= String.valueOf(Loanstatus.Pending);

       }
       else {
           loanStatus= String.valueOf(Loanstatus.Rejected);
       }


       LoanResponseDTO responseDTO = new LoanResponseDTO(loanrquest.getAmount(),loanrquest.getCustomerNumber(),loanStatus,limited_amount);
       responseDTO.setCustomerNumber(loansave.getCustomerNumber());
               return ResponseEntity.ok(responseDTO);
   }
}
