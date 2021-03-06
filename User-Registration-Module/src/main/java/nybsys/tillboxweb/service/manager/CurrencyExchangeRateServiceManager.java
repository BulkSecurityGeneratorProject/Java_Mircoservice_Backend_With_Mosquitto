/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/9/2018
 * Time: 11:10 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.service.manager;

import nybsys.tillboxweb.BaseService;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.bll.manager.CurrencyExchangeRateBllManager;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreModels.CurrencyExchangeRateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CurrencyExchangeRateServiceManager extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(Core.class);
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    private CurrencyExchangeRateBllManager currencyExchangeRateBllManager;

    public ResponseMessage save(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        this.currencyExchangeRateBllManager = new CurrencyExchangeRateBllManager();
        CurrencyExchangeRateModel currencyExchangeRateModel = new CurrencyExchangeRateModel();
        try {
            currencyExchangeRateModel = Core.getRequestObject(requestMessage, CurrencyExchangeRateModel.class);

            /*Set<ConstraintViolation<BusinessModel>> violations = this.validator.validate(this.businessModel);
             for (ConstraintViolation<BusinessModel> violation : violations) {
                 log.error(violation.getMessage());
             }*/

            currencyExchangeRateModel = this.currencyExchangeRateBllManager.saveOrUpdate(currencyExchangeRateModel);

            responseMessage.responseObj = currencyExchangeRateModel;
            if (Core.clientMessage.get().messageCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.CURRENCY_EXCHANGE_RATE_SAVE_SUCCESSFULLY;
                this.commit();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage != null) {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                } else {
                    responseMessage.message = MessageConstant.CURRENCY_EXCHANGE_RATE_SAVE_FAILED;
                }
                this.rollBack();
            }
        } catch (Exception ex) {
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
            this.WriteExceptionLog(ex);
            this.rollBack();
            log.error("CurrencyExchangeRateServiceManager -> save got exception");
        }
        return responseMessage;
    }


    public ResponseMessage getByID(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        this.currencyExchangeRateBllManager = new CurrencyExchangeRateBllManager();
        CurrencyExchangeRateModel currencyExchangeRateModel = new CurrencyExchangeRateModel();
        try {
            currencyExchangeRateModel = Core.getRequestObject(requestMessage, CurrencyExchangeRateModel.class);

            currencyExchangeRateModel = this.currencyExchangeRateBllManager.getById(currencyExchangeRateModel.getCurrencyID());
            responseMessage.responseObj = currencyExchangeRateModel;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;

        } catch (Exception ex) {
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
            this.WriteExceptionLog(ex);
            log.error("CurrencyExchangeRateServiceManager -> getByID got exception");

        }
        return responseMessage;
    }

    public ResponseMessage search(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        this.currencyExchangeRateBllManager = new CurrencyExchangeRateBllManager();
        CurrencyExchangeRateModel currencyExchangeRateModel = new CurrencyExchangeRateModel();
        List<CurrencyExchangeRateModel> lstCurrencyExchangeRateModel;
        try {
            currencyExchangeRateModel = Core.getRequestObject(requestMessage, CurrencyExchangeRateModel.class);

            lstCurrencyExchangeRateModel = this.currencyExchangeRateBllManager.getAllByConditions(currencyExchangeRateModel);
            responseMessage.responseObj = lstCurrencyExchangeRateModel;
            responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;

        } catch (Exception ex) {
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
            this.WriteExceptionLog(ex);
            log.error("CurrencyExchangeRateServiceManager -> search got exception");
        }
        return responseMessage;
    }

}
