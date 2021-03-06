/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/26/2018
 * Time: 10:14 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.service.manager;

import nybsys.tillboxweb.BaseService;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.Utils.TillBoxUtils;
import nybsys.tillboxweb.bll.manager.PurchaseOrderBllManager;
import nybsys.tillboxweb.broker.client.CallBack;
import nybsys.tillboxweb.broker.client.PublisherForWorker;
import nybsys.tillboxweb.broker.client.SubscriberForWorker;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.MessageModel.RequestMessage;
import nybsys.tillboxweb.MessageModel.ResponseMessage;
import nybsys.tillboxweb.constant.WorkerSubscriptionConstants;
import nybsys.tillboxweb.coreConstant.CurrencyConstant;
import nybsys.tillboxweb.coreModels.CurrencyModel;
import nybsys.tillboxweb.models.PurchaseOrderModel;
import nybsys.tillboxweb.models.SupplierModel;
import nybsys.tillboxweb.models.VMPurchaseOrder;
import nybsys.tillboxweb.models.VMPurchaseOrderList;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PurchaseOrderServiceManager extends BaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseOrderServiceManager.class);

    @Autowired
    private PurchaseOrderBllManager purchaseOrderBllManager = new PurchaseOrderBllManager();


    public ResponseMessage save(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        VMPurchaseOrder vmPurchaseOrder = new VMPurchaseOrder();
        SupplierModel supplierModel = new SupplierModel();


        try {
            vmPurchaseOrder = Core.getRequestObject(requestMessage, VMPurchaseOrder.class);
            /*Set<ConstraintViolation<AddressTypeModel>> violations = this.validator.validate(supplierAddressTypeModel);
            if (violations.size() > 0) {
                responseMessage = this.buildResponseMessage(requestMessage.requestObj, MessageConstant.modelViolation, TillBoxAppConstant.UN_PROCESSABLE_REQUEST);
                return responseMessage;
            }*/

            //(1)
            CurrencyModel currencyModel;
            currencyModel = getBaseCurrency();
            if (currencyModel == null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                responseMessage.message = CurrencyConstant.BASE_CURRENT_NOT_FOUND;
                Core.clientMessage.get().userMessage = CurrencyConstant.BASE_CURRENT_NOT_FOUND;
                return responseMessage;
            } else {
                currencyModel.setExchangeRate(1.00);
            }

            vmPurchaseOrder.purchaseOrderModel.setBaseCurrencyID(currencyModel.getCurrencyID());
            vmPurchaseOrder.purchaseOrderModel.setEntryCurrencyID(currencyModel.getCurrencyID());
            vmPurchaseOrder.purchaseOrderModel.setExchangeRate(currencyModel.getExchangeRate());
            vmPurchaseOrder.purchaseOrderModel.setBaseCurrencyAmount(vmPurchaseOrder.purchaseOrderModel.getTotal() * currencyModel.getExchangeRate());


            this.purchaseOrderBllManager.savePurchaseOrder(vmPurchaseOrder, requestMessage.businessID);
            if (Core.clientMessage.get().messageCode != null) {
                responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                if (Core.clientMessage.get().userMessage == null) {
                    responseMessage.message = MessageConstant.PURCHASE_ORDER_SAVE_FAILED;
                } else {
                    responseMessage.message = Core.clientMessage.get().userMessage;
                }
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.PURCHASE_ORDER_SAVE_SUCCESSFULLY;
            }
            responseMessage.responseObj = vmPurchaseOrder;

        } catch (Exception ex) {
            log.error("PurchaseOrderServiceManager -> savePurchaseOrderVM got exception");
            if (responseMessage.message == null) {
                responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            }
            this.WriteExceptionLog(ex);
            this.rollBack();
        }

        return responseMessage;
    }


    public CurrencyModel getBaseCurrency() {
        MqttClient mqttClient;
        ResponseMessage responseMessage = new ResponseMessage();
        CurrencyModel currencyModel = new CurrencyModel();
        RequestMessage reqMessForWorker;
        boolean workCompleteWithInAllowTime;
        try {

            Object lockObject = new Object();
            this.barrier = TillBoxUtils.getBarrier(1, lockObject);

            CallBack callBack;
            reqMessForWorker = Core.getDefaultWorkerRequestMessage();

            String pubTopic = WorkerSubscriptionConstants.WORKER_USER_REGISTRATION_MODULE_TOPIC;
            reqMessForWorker.brokerMessage.serviceName = "api/currency/getBaseCurrency";
            reqMessForWorker.token = Core.requestToken.get();

            SubscriberForWorker subForWorker = new SubscriberForWorker(reqMessForWorker.brokerMessage.messageId, this.barrier);
            mqttClient = subForWorker.subscribe();
            callBack = subForWorker.getCallBack();
            PublisherForWorker pubForWorker = new PublisherForWorker(pubTopic, mqttClient);
            pubForWorker.publishedMessageToWorker(reqMessForWorker);

            synchronized (lockObject) {
                long startTime = System.nanoTime();
                lockObject.wait(allowedTime);
                workCompleteWithInAllowTime = this.isResponseWithInAllowedTime(startTime);

                if (workCompleteWithInAllowTime) {
                    responseMessage = callBack.getResponseMessage();
                    if (responseMessage.responseObj != null) {
                        currencyModel = Core.jsonMapper.convertValue(responseMessage.responseObj, CurrencyModel.class);
                    } else {
                        currencyModel = null;
                    }
                } else {
                    //timeout
                    //TODO Implement role back logic
                    currencyModel = null;
                }
            }
            this.closeBrokerClient(mqttClient, reqMessForWorker.brokerMessage.messageId);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("OpeningBalanceServiceManager -> inter module communication getBaseCurrencyAndExchangeRate got exception");
        }
        return currencyModel;
    }


    public ResponseMessage getPurchaseOrderList(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        PurchaseOrderModel purchaseOrderModel = new PurchaseOrderModel();

        try {
            purchaseOrderModel = Core.getRequestObject(requestMessage, PurchaseOrderModel.class);
            List<VMPurchaseOrderList> lstVMPurchaseOrderList = new ArrayList<>();
            purchaseOrderModel.setBusinessID(requestMessage.businessID);
            lstVMPurchaseOrderList = this.purchaseOrderBllManager.getPurchaseOrderList(purchaseOrderModel);
            responseMessage.responseObj = lstVMPurchaseOrderList;

            if (lstVMPurchaseOrderList.size() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.NO_DATA_FOUND;
            }

        } catch (Exception ex) {
            log.error("PurchaseOrderServiceManager -> search purchase order vm got exception");
            if (responseMessage.message == null) {
                responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            }
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage search(RequestMessage requestMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        PurchaseOrderModel purchaseOrderModel = new PurchaseOrderModel();

        try {
            purchaseOrderModel = Core.getRequestObject(requestMessage, PurchaseOrderModel.class);
            List<VMPurchaseOrder> lstVMPurchaseOrder = new ArrayList<>();
            lstVMPurchaseOrder = this.purchaseOrderBllManager.searchVMPurchaseOrder(purchaseOrderModel);
            responseMessage.responseObj = lstVMPurchaseOrder;

            if (lstVMPurchaseOrder.size() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.NO_DATA_FOUND;
            }

        } catch (Exception ex) {
            log.error("PurchaseOrderServiceManager -> search purchase order vm got exception");
            if (responseMessage.message == null) {
                responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            }
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage getByID(RequestMessage requestMessage) {

        ResponseMessage responseMessage = new ResponseMessage();
        PurchaseOrderModel purchaseOrderModel = new PurchaseOrderModel();
        try {

            purchaseOrderModel = Core.getRequestObject(requestMessage, PurchaseOrderModel.class);
            VMPurchaseOrder vmPurchaseOrder = new VMPurchaseOrder();
            List<VMPurchaseOrder> lstvmPurchaseOrder = new ArrayList<>();
            purchaseOrderModel.setBusinessID(requestMessage.businessID);
            lstvmPurchaseOrder = this.purchaseOrderBllManager.searchVMPurchaseOrder(purchaseOrderModel);
            if (lstvmPurchaseOrder.size() > 0) {
                vmPurchaseOrder = lstvmPurchaseOrder.get(0);
            }

            responseMessage.responseObj = vmPurchaseOrder;
            if (lstvmPurchaseOrder.size() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
            } else {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.NO_DATA_FOUND;
            }

        } catch (Exception ex) {
            log.error("PurchaseOrderServiceManager -> search purchase order vm got exception");
            if (responseMessage.message == null) {
                responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            }
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }

    public ResponseMessage delete(RequestMessage requestMessage) {

        ResponseMessage responseMessage = new ResponseMessage();
        PurchaseOrderModel purchaseOrderModel = new PurchaseOrderModel();
        try {
            purchaseOrderModel = Core.getRequestObject(requestMessage, PurchaseOrderModel.class);
            purchaseOrderModel = this.purchaseOrderBllManager.deletePurchaseOrder(purchaseOrderModel);

            responseMessage.responseObj = purchaseOrderModel;
            if (purchaseOrderModel != null && purchaseOrderModel.getPurchaseOrderID() > 0) {
                responseMessage.responseCode = TillBoxAppConstant.SUCCESS_CODE;
                responseMessage.message = MessageConstant.DELETED_SUCCESSFULLY;
            } else {
                if (Core.clientMessage.get().userMessage != null) {
                    responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    responseMessage.message = Core.clientMessage.get().userMessage;
                } else {
                    responseMessage.responseCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    responseMessage.message = MessageConstant.FAILED_TO_DELETE;
                }
            }

        } catch (Exception ex) {
            log.error("PurchaseOrderServiceManager -> search purchase order delete exception");
            if (responseMessage.message == null) {
                responseMessage = this.getDefaultResponseMessage(requestMessage.requestObj, TillBoxAppConstant.INTERNAL_SERVER_ERROR, TillBoxAppConstant.INTERNAL_SERVER_ERROR_CODE);
            }
            this.WriteExceptionLog(ex);
        }

        return responseMessage;
    }


}
