package net.opengis.xls.v_1_2_0;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.lang.CopyStrategy;
import org.jvnet.jaxb2_commons.lang.CopyTo;
import org.jvnet.jaxb2_commons.lang.Equals;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy;
import org.jvnet.jaxb2_commons.lang.HashCode;
import org.jvnet.jaxb2_commons.lang.HashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBCopyStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBHashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBMergeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.MergeFrom;
import org.jvnet.jaxb2_commons.lang.MergeStrategy;
import org.jvnet.jaxb2_commons.lang.ToString;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * Defines an address
 * 
 * <p>Java class for AddressType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AddressType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/xls}AbstractAddressType">
 *       &lt;choice>
 *         &lt;element name="freeFormAddress" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;sequence>
 *           &lt;element ref="{http://www.opengis.net/xls}StreetAddress"/>
 *           &lt;element ref="{http://www.opengis.net/xls}Place" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{http://www.opengis.net/xls}PostalCode" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AddressType", propOrder = {
    "freeFormAddress",
    "streetAddress",
    "place",
    "postalCode"
})
public class AddressType
    extends AbstractAddressType
    implements Cloneable, CopyTo, Equals, HashCode, MergeFrom, ToString
{

    @XmlElement(name = "FreeFormAddress")    
    protected String freeFormAddress;
    @XmlElement(name = "StreetAddress")
    protected StreetAddressType streetAddress;
    @XmlElement(name = "Place")
    protected List<NamedPlaceType> place;
    @XmlElement(name = "PostalCode")
    protected String postalCode;

    /**
     * Gets the value of the freeFormAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFreeFormAddress() {
        return freeFormAddress;
    }

    /**
     * Sets the value of the freeFormAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFreeFormAddress(String value) {
        this.freeFormAddress = value;
    }

    /**
     * Gets the value of the streetAddress property.
     * 
     * @return
     *     possible object is
     *     {@link StreetAddressType }
     *     
     */
    public StreetAddressType getStreetAddress() {
        return streetAddress;
    }

    /**
     * Sets the value of the streetAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link StreetAddressType }
     *     
     */
    public void setStreetAddress(StreetAddressType value) {
        this.streetAddress = value;
    }

    /**
     * Gets the value of the place property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the place property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NamedPlaceType }
     * 
     * 
     */
    public List<NamedPlaceType> getPlace() {
        if (place == null) {
            place = new ArrayList<NamedPlaceType>();
        }
        return this.place;
    }

    /**
     * Gets the value of the postalCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the value of the postalCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostalCode(String value) {
        this.postalCode = value;
    }

    public String toString() {
        final ToStringStrategy strategy = JAXBToStringStrategy.INSTANCE;
        final StringBuilder buffer = new StringBuilder();
        append(null, buffer, strategy);
        return buffer.toString();
    }

    public StringBuilder append(ObjectLocator locator, StringBuilder buffer, ToStringStrategy strategy) {
        strategy.appendStart(locator, this, buffer);
        appendFields(locator, buffer, strategy);
        strategy.appendEnd(locator, this, buffer);
        return buffer;
    }

    public StringBuilder appendFields(ObjectLocator locator, StringBuilder buffer, ToStringStrategy strategy) {
        super.appendFields(locator, buffer, strategy);
        {
            String theFreeFormAddress;
            theFreeFormAddress = this.getFreeFormAddress();
            strategy.appendField(locator, this, "freeFormAddress", buffer, theFreeFormAddress);
        }
        {
            StreetAddressType theStreetAddress;
            theStreetAddress = this.getStreetAddress();
            strategy.appendField(locator, this, "streetAddress", buffer, theStreetAddress);
        }
        {
            List<NamedPlaceType> thePlace;
            thePlace = this.getPlace();
            strategy.appendField(locator, this, "place", buffer, thePlace);
        }
        {
            String thePostalCode;
            thePostalCode = this.getPostalCode();
            strategy.appendField(locator, this, "postalCode", buffer, thePostalCode);
        }
        return buffer;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy strategy) {
        if (!(object instanceof AddressType)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!super.equals(thisLocator, thatLocator, object, strategy)) {
            return false;
        }
        final AddressType that = ((AddressType) object);
        {
            String lhsFreeFormAddress;
            lhsFreeFormAddress = this.getFreeFormAddress();
            String rhsFreeFormAddress;
            rhsFreeFormAddress = that.getFreeFormAddress();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "freeFormAddress", lhsFreeFormAddress), LocatorUtils.property(thatLocator, "freeFormAddress", rhsFreeFormAddress), lhsFreeFormAddress, rhsFreeFormAddress)) {
                return false;
            }
        }
        {
            StreetAddressType lhsStreetAddress;
            lhsStreetAddress = this.getStreetAddress();
            StreetAddressType rhsStreetAddress;
            rhsStreetAddress = that.getStreetAddress();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "streetAddress", lhsStreetAddress), LocatorUtils.property(thatLocator, "streetAddress", rhsStreetAddress), lhsStreetAddress, rhsStreetAddress)) {
                return false;
            }
        }
        {
            List<NamedPlaceType> lhsPlace;
            lhsPlace = this.getPlace();
            List<NamedPlaceType> rhsPlace;
            rhsPlace = that.getPlace();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "place", lhsPlace), LocatorUtils.property(thatLocator, "place", rhsPlace), lhsPlace, rhsPlace)) {
                return false;
            }
        }
        {
            String lhsPostalCode;
            lhsPostalCode = this.getPostalCode();
            String rhsPostalCode;
            rhsPostalCode = that.getPostalCode();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "postalCode", lhsPostalCode), LocatorUtils.property(thatLocator, "postalCode", rhsPostalCode), lhsPostalCode, rhsPostalCode)) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    public int hashCode(ObjectLocator locator, HashCodeStrategy strategy) {
        int currentHashCode = super.hashCode(locator, strategy);
        {
            String theFreeFormAddress;
            theFreeFormAddress = this.getFreeFormAddress();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "freeFormAddress", theFreeFormAddress), currentHashCode, theFreeFormAddress);
        }
        {
            StreetAddressType theStreetAddress;
            theStreetAddress = this.getStreetAddress();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "streetAddress", theStreetAddress), currentHashCode, theStreetAddress);
        }
        {
            List<NamedPlaceType> thePlace;
            thePlace = this.getPlace();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "place", thePlace), currentHashCode, thePlace);
        }
        {
            String thePostalCode;
            thePostalCode = this.getPostalCode();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "postalCode", thePostalCode), currentHashCode, thePostalCode);
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

    public Object clone() {
        return copyTo(createNewInstance());
    }

    public Object copyTo(Object target) {
        final CopyStrategy strategy = JAXBCopyStrategy.INSTANCE;
        return copyTo(null, target, strategy);
    }

    public Object copyTo(ObjectLocator locator, Object target, CopyStrategy strategy) {
        final Object draftCopy = ((target == null)?createNewInstance():target);
        super.copyTo(locator, draftCopy, strategy);
        if (draftCopy instanceof AddressType) {
            final AddressType copy = ((AddressType) draftCopy);
            if (this.freeFormAddress!= null) {
                String sourceFreeFormAddress;
                sourceFreeFormAddress = this.getFreeFormAddress();
                String copyFreeFormAddress = ((String) strategy.copy(LocatorUtils.property(locator, "freeFormAddress", sourceFreeFormAddress), sourceFreeFormAddress));
                copy.setFreeFormAddress(copyFreeFormAddress);
            } else {
                copy.freeFormAddress = null;
            }
            if (this.streetAddress!= null) {
                StreetAddressType sourceStreetAddress;
                sourceStreetAddress = this.getStreetAddress();
                StreetAddressType copyStreetAddress = ((StreetAddressType) strategy.copy(LocatorUtils.property(locator, "streetAddress", sourceStreetAddress), sourceStreetAddress));
                copy.setStreetAddress(copyStreetAddress);
            } else {
                copy.streetAddress = null;
            }
            if ((this.place!= null)&&(!this.place.isEmpty())) {
                List<NamedPlaceType> sourcePlace;
                sourcePlace = this.getPlace();
                @SuppressWarnings("unchecked")
                List<NamedPlaceType> copyPlace = ((List<NamedPlaceType> ) strategy.copy(LocatorUtils.property(locator, "place", sourcePlace), sourcePlace));
                copy.place = null;
                List<NamedPlaceType> uniquePlacel = copy.getPlace();
                uniquePlacel.addAll(copyPlace);
            } else {
                copy.place = null;
            }
            if (this.postalCode!= null) {
                String sourcePostalCode;
                sourcePostalCode = this.getPostalCode();
                String copyPostalCode = ((String) strategy.copy(LocatorUtils.property(locator, "postalCode", sourcePostalCode), sourcePostalCode));
                copy.setPostalCode(copyPostalCode);
            } else {
                copy.postalCode = null;
            }
        }
        return draftCopy;
    }

    public Object createNewInstance() {
        return new AddressType();
    }

    public void mergeFrom(Object left, Object right) {
        final MergeStrategy strategy = JAXBMergeStrategy.INSTANCE;
        mergeFrom(null, null, left, right, strategy);
    }

    public void mergeFrom(ObjectLocator leftLocator, ObjectLocator rightLocator, Object left, Object right, MergeStrategy strategy) {
        super.mergeFrom(leftLocator, rightLocator, left, right, strategy);
        if (right instanceof AddressType) {
            final AddressType target = this;
            final AddressType leftObject = ((AddressType) left);
            final AddressType rightObject = ((AddressType) right);
            {
                String lhsFreeFormAddress;
                lhsFreeFormAddress = leftObject.getFreeFormAddress();
                String rhsFreeFormAddress;
                rhsFreeFormAddress = rightObject.getFreeFormAddress();
                target.setFreeFormAddress(((String) strategy.merge(LocatorUtils.property(leftLocator, "freeFormAddress", lhsFreeFormAddress), LocatorUtils.property(rightLocator, "freeFormAddress", rhsFreeFormAddress), lhsFreeFormAddress, rhsFreeFormAddress)));
            }
            {
                StreetAddressType lhsStreetAddress;
                lhsStreetAddress = leftObject.getStreetAddress();
                StreetAddressType rhsStreetAddress;
                rhsStreetAddress = rightObject.getStreetAddress();
                target.setStreetAddress(((StreetAddressType) strategy.merge(LocatorUtils.property(leftLocator, "streetAddress", lhsStreetAddress), LocatorUtils.property(rightLocator, "streetAddress", rhsStreetAddress), lhsStreetAddress, rhsStreetAddress)));
            }
            {
                List<NamedPlaceType> lhsPlace;
                lhsPlace = leftObject.getPlace();
                List<NamedPlaceType> rhsPlace;
                rhsPlace = rightObject.getPlace();
                target.place = null;
                List<NamedPlaceType> uniquePlacel = target.getPlace();
                uniquePlacel.addAll(((List<NamedPlaceType> ) strategy.merge(LocatorUtils.property(leftLocator, "place", lhsPlace), LocatorUtils.property(rightLocator, "place", rhsPlace), lhsPlace, rhsPlace)));
            }
            {
                String lhsPostalCode;
                lhsPostalCode = leftObject.getPostalCode();
                String rhsPostalCode;
                rhsPostalCode = rightObject.getPostalCode();
                target.setPostalCode(((String) strategy.merge(LocatorUtils.property(leftLocator, "postalCode", lhsPostalCode), LocatorUtils.property(rightLocator, "postalCode", rhsPostalCode), lhsPostalCode, rhsPostalCode)));
            }
        }
    }

    public void setPlace(List<NamedPlaceType> value) {
        List<NamedPlaceType> draftl = this.getPlace();
        draftl.addAll(value);
    }

}
