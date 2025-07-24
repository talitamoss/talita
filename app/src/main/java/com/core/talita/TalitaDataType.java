package com.core.talita;

/**
 * TalitaDataType - Bridge interface for legacy compatibility
 * 
 * This interface extends UniversalDataType to maintain compatibility
 * with existing code while transitioning to the universal system.
 * 
 * @deprecated Use UniversalDataType directly for new implementations
 */
public interface TalitaDataType extends UniversalDataType {
    // This interface exists for backward compatibility
    // All methods are inherited from UniversalDataType
}
