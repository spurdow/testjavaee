package com.imagecrawler.utils;

import org.apache.commons.validator.routines.UrlValidator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Created by davidluvellejoseph on 5/22/16.
 */
@FacesValidator("com.imagecrawler.utils.FUrlValidator")
public class FUrlValidator implements Validator {
    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        final String value = o.toString();
        UrlValidator urlValidator = new UrlValidator();
        if(urlValidator.isValid(value)){

        }else{
            FacesMessage msg =
                    new FacesMessage("URL validation failed","Invalid URL format");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }

    }
}
