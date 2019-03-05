/*
 * MATLAB Compiler: 7.0 (R2018b)
 * Date: Mon Feb 25 17:56:01 2019
 * Arguments: 
 * "-B""macro_default""-W""java:ReportLib,ReportUtils""-T""link:lib""-d""D:\\agodemar\\github\\jpad\\libraries\\report_generator\\matlab\\ReportLib\\for_testing""class{ReportUtils:D:\\agodemar\\github\\jpad\\libraries\\report_generator\\matlab\\generateReport.m}"
 */

package ReportLib;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;

/**
 * <i>INTERNAL USE ONLY</i>
 */
public class ReportLibMCRFactory
{
   
    
    /** Component's uuid */
    private static final String sComponentId = "ReportLib_3A0D101C3383A5049DA024563EA44749";
    
    /** Component name */
    private static final String sComponentName = "ReportLib";
    
   
    /** Pointer to default component options */
    private static final MWComponentOptions sDefaultComponentOptions = 
        new MWComponentOptions(
            MWCtfExtractLocation.EXTRACT_TO_CACHE, 
            new MWCtfClassLoaderSource(ReportLibMCRFactory.class)
        );
    
    
    private ReportLibMCRFactory()
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
            ReportLibMCRFactory.class, 
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
