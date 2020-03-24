package edu.asu.sbs.globals;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class AccountTypeAttributeConverter implements
        AttributeConverter<AccountType, String> {

    @Override
    public String convertToDatabaseColumn(AccountType attribute) {
        if (attribute == null)
            return null;
        return attribute.name();
    }

    @Override
    public AccountType convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return AccountType.valueOf(dbData);

    }
}
