package net.frontuari.utils;

import org.eevolution.model.MHRAttribute;
import org.eevolution.model.MHRMovement;
import org.eevolution.model.X_HR_Concept;

/**
 * @author <a href="mailto:jcolmenarez@frontuari.net">Jorge Colmenarez</a>
 *
 */
public class FactorMovement {

	private MHRMovement movement = null;
	private MHRAttribute attribute = null;
	
	/**
	 * *** Constructor ***
	 * @author <a href="mailto:jcolmenarez@frontuari.net">Jorge Colmenarez</a> 2020-11-23 21:12
	 */
	public FactorMovement(MHRMovement movement, MHRAttribute attribute) {
		this.movement = movement;
		this.attribute = attribute;
	}
	
	/**
	 * Get Movement
	 * @author <a href="mailto:jcolmenarez@frontuari.net">Jorge Colmenarez</a> 2020-11-23 21:12
	 * @return
	 * @return MHRMovement
	 */
	public MHRMovement getHR_Movement(){
		return movement;
	}

	/**
	 * Get Attribute
	 * @author <a href="mailto:jcolmenarez@frontuari.net">Jorge Colmenarez</a> 2020-11-23 21:12
	 * @return
	 * @return MHRAttribute
	 */
	public MHRAttribute getHR_Attribute(){
		return attribute;
	}
	
	/**
	 * Get Movement on Double format
	 * @author <a href="mailto:jcolmenarez@frontuari.net">Jorge Colmenarez</a> 2020-11-23 21:12
	 * @return
	 * @return double
	 */
	public double getDoubleMovement(){
		if(movement == null)
			return 0;
		double amt = 0;
		String columntype = movement.getColumnType();
		if(columntype.equals(X_HR_Concept.COLUMNTYPE_Amount))
			amt = movement.getAmount().doubleValue();
		else if(columntype.equals(X_HR_Concept.COLUMNTYPE_Quantity))
			amt = movement.getQty().doubleValue();
		return amt;
	}
	
	/**
	 * Get Attribute on Double
	 * @author <a href="mailto:jcolmenarez@frontuari.net">Jorge Colmenarez</a> 2020-11-23 21:12
	 * @return
	 * @return double
	 */ 
	public double getDoubleAttribute(){
		if(attribute == null)
			return 0;
		double amt = 0;
		String columntype = attribute.getColumnType();
		if(columntype.equals(X_HR_Concept.COLUMNTYPE_Amount))
			amt = attribute.getAmount().doubleValue();
		else if(columntype.equals(X_HR_Concept.COLUMNTYPE_Quantity))
			amt = attribute.getQty().doubleValue();
		return amt;
	}
}
