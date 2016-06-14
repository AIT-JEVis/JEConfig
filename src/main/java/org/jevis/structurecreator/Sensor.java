/**
 * Copyright (C) 2015 WernerLamprecht <werner.lamprecht@ymail.com>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * This class is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.structurecreator;

import java.io.Serializable;

/**
 * This class is used for collecting Wiotech sensor details
 * 
 * TODO change unicode string for CO2 in convertAndSetUnit()
 */
public class Sensor implements Serializable{

    
    private static final long serialVersionUID = 6659106543797L;
    private String name;
    //private String prefix;
    /**
     * Human readable symbol
     * °C - Temp
     * ‰ - CO2
     * % - rH
     */
    private String symbol;
    private String unit;
    private String table;
    private String mac;

    /**
     * 
     * @param wiotechSensorableName The Sensor name with 'sensor_' as prefix
     * the mac address as name and the Wiotech unit number like
     * sensor_1234567890ABCDEF_1
     */
    public Sensor(String wiotechSensorTableName) {
        
        String[] sensorDetails = wiotechSensorTableName.split("_");
        this.unit = convertAndSetUnit(sensorDetails[2]);
        this.mac = sensorDetails[1];
        this.table = wiotechSensorTableName;
        this.name = mac;
        
    }
   /**
    * 
    * @return The Sensor name with 'sensor_' as prefix
    * the mac address as name and the Wiotech unit number like
    * sensor_1234567890ABCDEF_1
    */
    public String getTable(){
        return table;
    }
    
    public String getName() {
        return name;
    }

    private void setName(String name) {

        this.name = name;
    }

    /*public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }*/
    /**
     * 
     * @return human readable symbol
     */
    public String getSymbol() {
        return symbol;
    }
    
    /**
     * 
     * @param unitSymbol human readable symbol
     */
    private void setSymbol(String unitSymbol) {
        this.symbol = symbol;
    }
    
    /**
     * 
     * @return Unicode unit like "\u00b0C" for °C
     */
    public String getUnit() {
        return unit;
    }
    
    /**
     * Converts the Wiotech unit number into a unicode string
     * @param wiotechUnitNumber
     * @return Unicode unit like "\u00b0C" for °C
     */
    private String convertAndSetUnit(String wiotechUnitNumber){
        switch(wiotechUnitNumber){
            case "1": //°C
                this.symbol="Temp";
                this.unit = "\u00b0C";
                return this.getUnit();
            case "6"://‰
                this.symbol="CO2";
                this.unit = "\u0025";
                return  this.getUnit();
            case "9":// %
                this.symbol="rH";
                this.unit = "\u0025";
                return this.getUnit();    
                
            default:
                return null;
        }
    }

    /**
     * @return the sensor's mac
     */
    public String getMac() {
        return mac;
    }
}