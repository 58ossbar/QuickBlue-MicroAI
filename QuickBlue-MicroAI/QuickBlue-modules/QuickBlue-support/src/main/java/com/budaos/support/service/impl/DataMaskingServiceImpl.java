package com.budaos.support.service.impl;

import com.budaos.support.service.DataMaskingService;
import org.springframework.stereotype.Service;

/**
 * 数据脱敏服务实现
 *
 * @author budaos
 */
@Service
public class DataMaskingServiceImpl implements DataMaskingService {

    @Override
    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    @Override
    public String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "***********" + idCard.substring(idCard.length() - 4);
    }

    @Override
    public String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) {
            return bankCard;
        }
        int length = bankCard.length();
        int maskLength = length - 8;
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < maskLength; i++) {
            mask.append("*");
        }
        return bankCard.substring(0, 4) + mask + bankCard.substring(length - 4);
    }

    @Override
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    @Override
    public String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < name.length() - 1; i++) {
            mask.append("*");
        }
        return name.charAt(0) + mask.toString();
    }

    @Override
    public String maskAddress(String address) {
        if (address == null || address.length() <= 6) {
            return address;
        }
        // 保留前6个字符（省市区），其余用*代替
        int maskLength = address.length() - 6;
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < Math.min(maskLength, 10); i++) {
            mask.append("*");
        }
        return address.substring(0, 6) + mask;
    }
}
