package org.obiba.onyx.jade.core.domain.instrument;

import javax.measure.unit.Unit;

import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit converter
 * @author acarey
 */

public final class UnitParameterValueConverter implements InstrumentParameterValueConverter {

  private static final Logger log = LoggerFactory.getLogger(UnitParameterValueConverter.class);

  /**
   * Convert the value from a source unit to a target unit Note: if the value is an age, the method adjusts the value to
   * return the right age
   * @param targetInstrumentRunValue
   * @param sourceInstrumentRunValue
   */
  @SuppressWarnings("unchecked")
  public void convert(InstrumentRunValue targetInstrumentRunValue, InstrumentRunValue sourceInstrumentRunValue) {

    log.debug("Converting parameters from source {} to target {}", sourceInstrumentRunValue.getInstrumentParameter().getName(), targetInstrumentRunValue.getInstrumentParameter().getName());

    Unit sourceUnit = Unit.valueOf(sourceInstrumentRunValue.getInstrumentParameter().getMeasurementUnit());
    Unit targetUnit = Unit.valueOf(targetInstrumentRunValue.getInstrumentParameter().getMeasurementUnit());

    log.debug("Converting units from source {} to target {}", sourceUnit.toString(), targetUnit.toString());

    double sourceValue;
    // Extract the source value and convert it to a double
    try {
      sourceValue = Double.parseDouble(sourceInstrumentRunValue.getData().getValueAsString());
    } catch(NumberFormatException e) {
      Data sourceData = sourceInstrumentRunValue.getData();
      log.error("Error converting between measurement units. Original value {} of type {} cannot be converted to a double, which is required to convert between measurement units.", sourceData.getValueAsString(), sourceData.getType());
      throw e;
    }

    double newValue = sourceUnit.getConverterTo(targetUnit).convert(sourceValue);

    switch(targetInstrumentRunValue.getInstrumentParameter().getDataType()) {
    case DECIMAL:
      targetInstrumentRunValue.setData(DataBuilder.buildDecimal(newValue));
      break;

    case INTEGER:
      if(targetUnit.toString().equalsIgnoreCase("year")) newValue = Math.floor(newValue);
      targetInstrumentRunValue.setData(DataBuilder.buildInteger(Math.round(newValue)));
      break;
    }

  }
}
