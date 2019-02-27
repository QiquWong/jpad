/*
 * MATLAB Compiler: 7.0 (R2018b)
 * Date: Wed Feb 27 15:24:01 2019
 * Arguments: 
 * "-B""macro_default""-W""java:MReportUtils,ReportMaker""-T""link:lib""-d""C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\examples\\MReportUtils\\for_testing""class{ReportMaker:C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\examples\\makeReport02.m}"
 */

package MReportUtils;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;

/**
 * <i>INTERNAL USE ONLY</i>
 */
public class MReportUtilsMCRFactory
{
   
    
    /** Component's uuid */
    private static final String sComponentId = "MReportUtils_22DB688481357353D26A8E592983749D";
    
    /** Component name */
    private static final String sComponentName = "MReportUtils";
    
   
    /** Pointer to default component options */
    private static final MWComponentOptions sDefaultComponentOptions = 
        new MWComponentOptions(
            MWCtfExtractLocation.EXTRACT_TO_CACHE, 
            new MWCtfClassLoaderSource(MReportUtilsMCRFactory.class)
        );
    
    
    private MReportUtilsMCRFactory()
    {
        // Never called.
    }
    
    public static MWMCR newInstance(MWComponentOptions componentOptions) throws MWException
    {
        if (null == componentOptions.getCtfSource()) {
            componentOptions = new MWComponentOptions(componentOptions);
            componentOptions.setCtfSource(sDefaultComponentOptions.getCtfSource());
        }
        return MWMCR.newInstance(
            componentOptions, 
            MReportUtilsMCRFactory.class, 
            sComponentName, 
            sComponentId,
            new int[]{9,5,0}
        );
    }
    
    public static MWMCR newInstance() throws MWException
    {
        return newInstance(sDefaultComponentOptions);
    }
}
