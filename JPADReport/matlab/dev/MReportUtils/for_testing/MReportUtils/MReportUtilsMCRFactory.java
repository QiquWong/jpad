/*
 * MATLAB Compiler: 7.0.1 (R2019a)
 * Date: Tue Apr 16 15:33:02 2019
 * Arguments: 
 * "-B""macro_default""-v""-K""-W""java:MReportUtils,MReportUtils""-T""link:lib""-d""C:\\Users\\Prince\\Tesi\\jpad\\JPADReport\\matlab\\dev\\MReportUtils\\for_testing""class{MReportUtils:C:\\Users\\Prince\\Tesi\\jpad\\JPADReport\\matlab\\dev\\closeChapter.m,C:\\Users\\Prince\\Tesi\\jpad\\JPADReport\\matlab\\dev\\closeReport.m,C:\\Users\\Prince\\Tesi\\jpad\\JPADReport\\matlab\\dev\\makeChapter.m,C:\\Users\\Prince\\Tesi\\jpad\\JPADReport\\matlab\\dev\\makeFigure.m,C:\\Users\\Prince\\Tesi\\jpad\\JPADReport\\matlab\\dev\\makeParagraph.m,C:\\Users\\Prince\\Tesi\\jpad\\JPADReport\\matlab\\dev\\makeReport.m,C:\\Users\\Prince\\Tesi\\jpad\\JPADReport\\matlab\\dev\\makeSection.m,C:\\Users\\Prince\\Tesi\\jpad\\JPADReport\\matlab\\dev\\makeTable.m}""-a""C:\\Users\\Prince\\Tesi\\jpad\\JPADReport\\matlab\\dev\\DAF_template.pdftx"
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
    private static final String sComponentId = "MReportUtils_69711B94C6C429214FC5DF5C6BAD4C9D";
    
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
            new int[]{9,6,0}
        );
    }
    
    public static MWMCR newInstance() throws MWException
    {
        return newInstance(sDefaultComponentOptions);
    }
}
