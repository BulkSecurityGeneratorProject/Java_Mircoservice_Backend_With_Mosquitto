/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 12/03/2018
 * Time: 12:07
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.service.manager;

import nybsys.tillboxweb.BaseService;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.bll.manager.CustomerCategoryBllManager;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreModels.CustomerCategoryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CustomerCategoryServiceManager extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(CustomerCategoryServiceManager.class);
    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    @Autowired
    private CustomerCategoryBllManager customerCategoryBllManager = new CustomerCategoryBllManager();

    public ResponseMessage saveCustomerCategory(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        CustomerCategoryModel customerCategoryModel = new CustomerCategoryModel();
        try {
            customerCategoryModel = Core.getRequestObject(requestMessage, CustomerCategoryModel.class);

            /*Set<ConstraintViolation<CustomerCategoryModel>> violations = this.validator.validate(customerCategoryModel);
            if (violations.size() > 0) {
                responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, MessageConstant.modelViolation, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
                return responseMessage;
            }*/
            customerCategoryModel.setBusinessID(requestMessage.businessID);
            customerCategoryModel = this.customerCategoryBllManager.saveOrUpdate(customerCategoryModel);

            responseMessage.responseObj = customerCategoryModel;
            if (Core.clientMessage.get().messageCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.CUSTOMER_CATEGORY_SAVE_SUCCESSFULLY;
                this.commit();
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.CUSTOMER_CATEGORY_SAVE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
                this.rollBack();
            }

        } catch (Exception ex) {
            log.error("CustomerCategoryServiceManager -> saveCustomerCategory got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }

    public ResponseMessage searchCustomerCategory(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        List<CustomerCategoryModel> lstCustomerCategoryModel = new ArrayList<>();
        CustomerCategoryModel customerCategoryModel = new CustomerCategoryModel();
        try {
            customerCategoryModel = Core.getRequestObject(requestMessage, CustomerCategoryModel.class);
            customerCategoryModel.setBusinessID(requestMessage.businessID);
            lstCustomerCategoryModel = this.customerCategoryBllManager.searchCustomerCategory(customerCategoryModel);

            responseMessage.responseObj = lstCustomerCategoryModel;
            if (Core.clientMessage.get().messageCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.CUSTOMER_CATEGORY_GET_SUCCESSFULLY;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.CUSTOMER_CATEGORY_GET_FAILED;
            }

        } catch (Exception ex) {
            log.error("CustomerCategoryServiceManager -> searchCustomerCategory got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage getCustomerCategoryByID(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        CustomerCategoryModel customerCategoryModel = new CustomerCategoryModel();
        try {
            customerCategoryModel = Core.getRequestObject(requestMessage, CustomerCategoryModel.class);

            customerCategoryModel = this.customerCategoryBllManager.getCustomerCategoryByID(customerCategoryModel.getCustomerCategoryID());

            responseMessage.responseObj = customerCategoryModel;
            if (Core.clientMessage.get().messageCode == null) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.CUSTOMER_CATEGORY_GET_SUCCESSFULLY;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = MessageConstant.CUSTOMER_CATEGORY_GET_FAILED;
            }

        } catch (Exception ex) {
            log.error("CustomerCategoryServiceManager -> getCustomerCategoryByID got exception");
            responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }
}
