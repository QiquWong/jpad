package aircraft.components;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.AircraftTypeEnum;

public class AircraftCreator {
	
	private Aircraft _theAircraft;
	
	public AircraftCreator() {
		_theAircraft = new Aircraft();
	}
	
	public void setAircraftProperties() {
		_theAircraft.set_name("AIRCRAFT");
		_theAircraft.set_typeVehicle(AircraftTypeEnum.TURBOPROP);
	}
	
	public void setConfiguration() {
		Configuration configuration = _theAircraft.get_configuration();
		configuration.set_nPax(72.0);
		configuration.set_maxPax(72.0);
		configuration.set_aislesNumber(1);
		configuration.set_classesNumber(1.0);
		configuration.set_xCoordinateFirstRow(Amount.valueOf(3.5, SI.METER));
		
	}
	
//	<AIRCRAFT>
//    <Configuration id="11">
//        <Number_of_passengers from="input" unit="" varName="_nPax">109.0000</Number_of_passengers>
//        <Maximum_number_of_passengers from="input" unit="" varName="_maxPax">109.0000</Maximum_number_of_passengers>
//        <Number_of_aisles from="input" unit="" varName="_aislesNumber">1</Number_of_aisles>
//        <Number_of_classes from="input" unit="" varName="_classesNumber">1.0000</Number_of_classes>
//        <Xcoordinate_of_first_row from="input" unit="m" varName="_xCoordinateFirstRow">7.4000</Xcoordinate_of_first_row>
//        <Cabin_Layout>
//            <Economy>
//                <Pitch from="input" unit="m" varName="_pitchEconomyClass">0.8000</Pitch>
//                <Width from="input" unit="m" varName="_widthEconomyClass">0.4000</Width>
//                <Abreasts from="input" unit="" varName="_numberOfColumnsEconomyClass">[2.0, 2.0]</Abreasts>
//                <Number_of_rows from="input" unit="" varName="_numberOfRowsEconomyClass">22</Number_of_rows>
//                <Distance_from_wall from="input" unit="m" varName="_distanceFromWallEconomyClass">0.1000</Distance_from_wall>
//                <Number_of_breaks from="input" unit="" varName="_numberOfBreaksEconomyClass">-1</Number_of_breaks>
//                <Length_of_each_break from="input" unit="" varName="_lengthOfEachBreakEconomyClass">[0.0, 0.0]</Length_of_each_break>
//            </Economy>
//            <Business>
//                <Pitch from="input" unit="m" varName="_pitchBusinessClass">0.0000</Pitch>
//                <Width from="input" unit="m" varName="_widthBusinessClass">0.0000</Width>
//                <Abreasts from="input" unit="" varName="_numberOfColumnsBusinessClass">[0.0, 0.0]</Abreasts>
//                <Number_of_rows from="input" unit="" varName="_numberOfRowsBusinessClass">0</Number_of_rows>
//                <Distance_from_wall from="input" unit="m" varName="_distanceFromWallBusinessClass">0.1000</Distance_from_wall>
//                <Number_of_breaks from="input" unit="" varName="_numberOfBreaksBusinessClass">-1</Number_of_breaks>
//                <Length_of_each_break from="input" unit="" varName="_lengthOfEachBreakBusinessClass">[0.0, 0.0]</Length_of_each_break>
//            </Business>
//            <First>
//                <Pitch from="input" unit="m" varName="_pitchFirstClass">0.0000</Pitch>
//                <Width from="input" unit="m" varName="_widthFirstClass">0.0000</Width>
//                <Abreasts from="input" unit="" varName="_numberOfColumnsFirstClass">[0.0, 0.0]</Abreasts>
//                <Number_of_rows from="input" unit="" varName="_numberOfRowsFirstClass">0</Number_of_rows>
//                <Distance_from_wall from="input" unit="m" varName="_distanceFromWallFirstClass">0.1000</Distance_from_wall>
//                <Number_of_breaks from="input" unit="" varName="_numberOfBreaksFirstClass">-1</Number_of_breaks>
//                <Length_of_each_break from="input" unit="" varName="_lengthOfEachBreakFirstClass">[0.0, 0.0]</Length_of_each_break>
//            </First>
//        </Cabin_Layout>
//    </Configuration>
//    <Performances id="21">
//        <Optimum_Cruise_Mach_Number from="input" unit="" varName="_machOptimumCruise">0.7600</Optimum_Cruise_Mach_Number>
//        <Maximum_Cruise_Mach_Number from="input" unit="" varName="_machMaxCruise">0.8000</Maximum_Cruise_Mach_Number>
//        <Limit_load_factor from="input" unit="" varName="_nLimit">2.5000</Limit_load_factor>
//        <Limit_load_factor_at_MZFW from="input" unit="" varName="_nLimitZFW">2.5000</Limit_load_factor_at_MZFW>
//        <Ultimate_load_factor from="input" unit="" varName="_nUltimate">3.8000</Ultimate_load_factor>
//    </Performances>
//    <Weights id="20">
//        <Material_density from="input" unit="kg/m³" varName="_materialDensity">2711.0000</Material_density>
//        <Maximum_zero_fuel_mass from="input" unit="kg" varName="_MZFM">35064.4216</Maximum_zero_fuel_mass>
//        <Maximum_landing_mass from="input" unit="kg" varName="_MLM">41180.7795</Maximum_landing_mass>
//        <Maximum_take_off_mass from="input" unit="kg" varName="_MTOM">45752.6550</Maximum_take_off_mass>
//    </Weights>
//    <Balance/>
//    <Fuselage id="0">
//        <Adjust_Criterion>NONE</Adjust_Criterion>
//        <Fuselage_Parameters>
//            <!--Main fuselage geometric parameters -->
//            <Length from="input" unit="m" varName="_len_F">32.5000</Length>
//            <Number_of_decks from="input" unit="">1</Number_of_decks>
//            <Nose_to_fuselage_lenght_ratio from="input" unit="" varName="_lenRatio_NF">0.1000</Nose_to_fuselage_lenght_ratio>
//            <Cylindrical_part_to_fuselage_lenght_ratio from="input" unit="" varName="_lenRatio_CF">0.6000</Cylindrical_part_to_fuselage_lenght_ratio>
//            <Cylindrical_part_width from="input" unit="m" varName="_sectionCylinderWidth">3.3000</Cylindrical_part_width>
//            <Cylindrical_part_height from="input" unit="m" varName="_sectionCylinderHeight">3.3000</Cylindrical_part_height>
//            <Nose_fineness_ratio from="input" unit="" varName="_lambda_N">1.5000</Nose_fineness_ratio>
//            <Minimum_height_from_ground from="input" unit="m" varName="_heightFromGround">1.5000</Minimum_height_from_ground>
//            <Surface_roughess from="input" unit="m" varName="_roughness">0.0000</Surface_roughess>
//            <Nose_furthermost_point_height from="input" unit="m" varName="_height_N">-0.7180</Nose_furthermost_point_height>
//            <Tail_rearmost_point_height from="input" unit="m" varName="_height_T">1.2000</Tail_rearmost_point_height>
//            <Nose_cap_lenght from="input" unit="m" varName="_dxNoseCap">0.2437</Nose_cap_lenght>
//            <Tail_cap_lenght from="input" unit="m" varName="_dxTailCap">0.1950</Tail_cap_lenght>
//            <Windshield_type from="input" unit="">Single,round</Windshield_type>
//            <Windshield_height from="input" unit="m" varName="_windshieldHeight">0.5000</Windshield_height>
//            <Windshield_width from="input" unit="m" varName="_windshieldWidth">3.0000</Windshield_width>
//            <Cylinder_lower_to_total_height_ratio from="input" unit="" varName="_sectionLowerToTotalHeightRatio">0.4000</Cylinder_lower_to_total_height_ratio>
//            <Cylinder_Rho_upper from="input" unit="" varName="_sectionCylinderRhoUpper">0.1000</Cylinder_Rho_upper>
//            <Cylinder_Rho_lower from="input" unit="" varName="_sectionCylinderRhoLower">0.1000</Cylinder_Rho_lower>
//            <Pressurization from="input" unit="" varName="_pressurized">true</Pressurization>
//            <Reference_mass from="input" unit="kg" varName="_massReference">3340.6000</Reference_mass>
//            <Mass_correction_factor from="input" unit="" varName="_massCorrectionFactor">1.0000</Mass_correction_factor>
//            <Nose_Length unit="m">3.2500</Nose_Length>
//            <Cylindrical_Length unit="m">19.5000</Cylindrical_Length>
//            <TailCone_Length unit="m">9.7500</TailCone_Length>
//            <Nose_Length_Ratio unit="">0.1000</Nose_Length_Ratio>
//            <Cylindrical_Length_Ratio unit="">0.6000</Cylindrical_Length_Ratio>
//            <TailCone_Length_Ratio unit="">0.3000</TailCone_Length_Ratio>
//            <Nose_Fineness_Ratio unit="">1.5000</Nose_Fineness_Ratio>
//            <Cylindrical_Fineness_Ratio unit="">5.9091</Cylindrical_Fineness_Ratio>
//            <TailCone_Fineness_Ratio unit="">2.9545</TailCone_Fineness_Ratio>
//        </Fuselage_Parameters>
//        <Fuse_Section_List>
//            <!--Fuselage Sections parameters. Edit these to alter the fuselage shape-->
//            <Fuse_Cylinder_Section_X_Location from="input" unit="m">3.2500</Fuse_Cylinder_Section_X_Location>
//            <Fuse_Cylinder_Section_Rho_Upper from="input" unit="" varName="_sectionCylinderRhoUpper">0.1000</Fuse_Cylinder_Section_Rho_Upper>
//            <Fuse_Cylinder_Section_Rho_Lower from="input" unit="" varName="_sectionCylinderRhoLower">0.1000</Fuse_Cylinder_Section_Rho_Lower>
//            <Fuse_Cylinder_Lower_to_total_height_ratio from="input" unit="" varName="_sectionLowerToTotalHeightRatio">0.4000</Fuse_Cylinder_Lower_to_total_height_ratio>
//            <Cylinder_Base_Area unit="m²">8.5530</Cylinder_Base_Area>
//            <Section_stations unit="m">0.2437</Section_stations>
//            <Nose_Cap_Section_Width unit="m">0.7830</Nose_Cap_Section_Width>
//            <Nose_Cap_Section_Height unit="m">0.7690</Nose_Cap_Section_Height>
//            <Nose_Cap_Section_Rho_Upper unit="">0.000000</Nose_Cap_Section_Rho_Upper>
//            <Nose_Cap_Section_Rho_Lower unit="">0.000000</Nose_Cap_Section_Rho_Lower>
//            <Nose_Cap_Lower_Section_a_Control_Point unit="">0.5000</Nose_Cap_Lower_Section_a_Control_Point>
//            <Mid_Nose_Section_X_Location unit="m">1.6250</Mid_Nose_Section_X_Location>
//            <Mid_Nose_Section_Width unit="m">2.3960</Mid_Nose_Section_Width>
//            <Mid_Nose_Section_Height unit="m">2.3420</Mid_Nose_Section_Height>
//            <Mid_Nose_Section_Rho_Upper unit="">0.2000</Mid_Nose_Section_Rho_Upper>
//            <Mid_Nose_Section_Rho_Lower unit="">0.3000</Mid_Nose_Section_Rho_Lower>
//            <Mid_Nose_Lower_Section_a_Control_Point unit="">0.4000</Mid_Nose_Lower_Section_a_Control_Point>
//            <Mid_Tail_Section_X_Location unit="m">27.6250</Mid_Tail_Section_X_Location>
//            <Mid_Tail_Section_Width unit="m">2.4690</Mid_Tail_Section_Width>
//            <Mid_Tail_Section_Height unit="m">2.5430</Mid_Tail_Section_Height>
//            <Mid_Tail_Section_Rho_Upper unit="">0.2000</Mid_Tail_Section_Rho_Upper>
//            <Mid_Tail_Section_Rho_Lower unit="">0.3000</Mid_Tail_Section_Rho_Lower>
//            <Mid_Tail_Lower_Section_a_Control_Point unit="">0.4000</Mid_Tail_Lower_Section_a_Control_Point>
//            <Tail_Cap_Section_X_Location unit="m">32.3050</Tail_Cap_Section_X_Location>
//            <Tail_Cap_Section_Width unit="m">0.4160</Tail_Cap_Section_Width>
//            <Tail_Cap_Section_Height unit="m">2.3420</Tail_Cap_Section_Height>
//            <Tail_Cap_Section_Rho_Upper unit="">0.000000</Tail_Cap_Section_Rho_Upper>
//            <Tail_Cap_Section_Rho_Lower unit="">0.000000</Tail_Cap_Section_Rho_Lower>
//            <Tail_Cap_Lower_Section_a_Control_Point unit="">0.5000</Tail_Cap_Lower_Section_a_Control_Point>
//        </Fuse_Section_List>
//    </Fuselage>
//    <Wing id="1">
//        <Equivalent_Wing_parameters>
//            <Xcoordinate from="input" unit="m" varName="_X0">13.5000</Xcoordinate>
//            <Ycoordinate from="input" unit="m" varName="_Y0">0.0000</Ycoordinate>
//            <Zcoordinate from="input" unit="m" varName="_Z0">-1.0800</Zcoordinate>
//            <Planform_surface from="input" unit="m²" varName="_surface">93.5000</Planform_surface>
//            <Control_surface_extension from="input" unit="m²" varName="_surfaceCS">23.4000</Control_surface_extension>
//            <aspectRatio from="input" unit="" varName="_aspectRatio">8.4000</aspectRatio>
//            <taperRatio from="input" unit="" varName="_taperRatioEquivalent">0.2000</taperRatio>
//            <Wing_position_in_percent_of_fuselage_height from="input" unit="" varName="_positionRelativeToAttachment">0.000000</Wing_position_in_percent_of_fuselage_height>
//            <kinkSpanStation from="input" unit="" varName="_spanStationKink">0.3000</kinkSpanStation>
//            <Thickness_to_chord_ratio_root from="input" unit="" varName="_tc_root">0.1600</Thickness_to_chord_ratio_root>
//            <Thickness_to_chord_ratio_kink from="input" unit="" varName="_tc_kink">0.1000</Thickness_to_chord_ratio_kink>
//            <Thickness_to_chord_ratio_tip from="input" unit="" varName="_tc_tip">0.0900</Thickness_to_chord_ratio_tip>
//            <Root_chord_LE_extension from="input" unit="" varName="_extensionLERootChordLinPanel">0.000000</Root_chord_LE_extension>
//            <Root_chord_TE_extension from="input" unit="" varName="_extensionTERootChordLinPanel">0.1000</Root_chord_TE_extension>
//            <sweepc4 from="input" unit="°" varName="_sweepQuarterChordEq">17.1887</sweepc4>
//            <Incidence_relative_to_xBRF from="input" unit="°" varName="_iw">0.0000</Incidence_relative_to_xBRF>
//            <kinkStationTwist from="input" unit="°" varName="_twistKink">-0.5000</kinkStationTwist>
//            <tipStationTwist from="input" unit="°" varName="_twistTip">-4.5000</tipStationTwist>
//            <Dihedral_inner_panel from="input" unit="°" varName="_dihedralInnerPanel">5.7296</Dihedral_inner_panel>
//            <Dihedral_outer_panel from="input" unit="°" varName="_dihedralOuterPanel">5.7296</Dihedral_outer_panel>
//            <Surface_roughness from="input" unit="m" varName="_roughness">0.0000</Surface_roughness>
//            <Transition_point_in_percent_of_chord_upper_wing from="input" unit="" varName="_xTransitionU">0.2000</Transition_point_in_percent_of_chord_upper_wing>
//            <Transition_point_in_percent_of_chord_upper_wing from="input" unit="" varName="_xTransitionL">0.1200</Transition_point_in_percent_of_chord_upper_wing>
//            <Reference_mass from="input" unit="kg" varName="_massReference">4669.0000</Reference_mass>
//            <Composite_correction_factor from="input" unit="" varName="_compositeCorretionFactor">0.000000</Composite_correction_factor>
//            <Mass_correction_factor from="input" unit="" varName="_massCorrectionFactor">1.0000</Mass_correction_factor>
//            <sweepLE unit="°">21.2411</sweepLE>
//            <Mean_aerodynamic_chord_MAC unit="m">3.8306</Mean_aerodynamic_chord_MAC>
//            <Root_chord unit="m">5.5605</Root_chord>
//        </Equivalent_Wing_parameters>
//        <Actual_Wing_parameters>
//            <Mach_number_transonic_threshold from="input" unit="" varName="_machTransonicThreshold">0.7000</Mach_number_transonic_threshold>
//            <Planform_surface unit="m²">93.5000</Planform_surface>
//            <Exposed_surface unit="m²">75.0857</Exposed_surface>
//            <Wetted_surface unit="m²">191.1086</Wetted_surface>
//            <Exposed_Wetted_surface unit="m²">153.4708</Exposed_Wetted_surface>
//            <span unit="m">28.0250</span>
//            <taperRatio unit="">0.1873</taperRatio>
//            <semiSurfaceInnerPanel unit="m²">21.1260</semiSurfaceInnerPanel>
//            <aspectRatioInnerPanel unit="">1.6730</aspectRatioInnerPanel>
//            <semiSurfaceOuterPanel unit="m²">25.6240</semiSurfaceOuterPanel>
//            <aspectRatioOuterPanel unit="">7.5095</aspectRatioOuterPanel>
//            <taperRatioInnerPanel unit="">0.6925</taperRatioInnerPanel>
//            <taperRatioOuterPanel unit="">0.2704</taperRatioOuterPanel>
//            <sweepLEInnerPanel unit="°">21.2411</sweepLEInnerPanel>
//            <sweepc4InnerPanel unit="°">15.6486</sweepc4InnerPanel>
//            <sweepLEOuterPanel unit="°">21.2411</sweepLEOuterPanel>
//            <sweepc4OuterPanel unit="°">17.3397</sweepc4OuterPanel>
//            <rootChord unit="m">5.9384</rootChord>
//            <rootChordXle unit="m">0.0000</rootChordXle>
//            <kinkChord unit="m">4.1126</kinkChord>
//            <kinkChordXle unit="m">1.6340</kinkChordXle>
//            <tipChord unit="m">1.1121</tipChord>
//            <tipChordXle unit="m">5.4467</tipChordXle>
//            <Mean_aerodynamic_chord_MAC unit="m">3.8872</Mean_aerodynamic_chord_MAC>
//            <Mean_aerodynamic_chord_xLE_LRF unit="m">2.0858</Mean_aerodynamic_chord_xLE_LRF>
//            <Mean_aerodynamic_chord_yLE_LRF unit="m">5.3662</Mean_aerodynamic_chord_yLE_LRF>
//            <Mean_aerodynamic_chord_xLE_BRF unit="m">15.5858</Mean_aerodynamic_chord_xLE_BRF>
//            <Mean_aerodynamic_chord_yLE_BRF unit="m">5.3662</Mean_aerodynamic_chord_yLE_BRF>
//            <Mean_dihedral_angle unit="°">5.7296</Mean_dihedral_angle>
//            <AC_to_CG_distance unit="">not_initialized</AC_to_CG_distance>
//            <AC_to_Wing_AC_distance unit="">not_initialized</AC_to_Wing_AC_distance>
//            <Volumetric_ratio unit="">not_initialized</Volumetric_ratio>
//            <Mean_maximum_thickness>
//                <Integral_mean unit="">0.1152</Integral_mean>
//            </Mean_maximum_thickness>
//            <Number_of_airfoils unit="">3</Number_of_airfoils>
//            <Airfoil_1 id="1099" level="3">
//                <Family from="input" unit="" varName="_family">NACA63_209</Family>
//                <Type from="input" unit="" varName="_type">SUPERCRITICAL</Type>
//                <Airfoil_Geometry id="10990099" level="4">
//                    <Position_along_semispan unit="">0.000000</Position_along_semispan>
//                    <Twist_relative_to_root from="input" unit="°" varName="_twist">0.0000</Twist_relative_to_root>
//                    <Thickness_to_chord_ratio_max from="input" unit="" varName="_maximumThicknessOverChord">0.1500</Thickness_to_chord_ratio_max>
//                    <Xcoordinate from="input" unit="" varName="_xCoords">[1.0, 0.9506, 0.9012, 0.85162, 0.80185, 0.75191, 0.70178, 0.65149, 0.60107, 0.55056, 0.5, 0.44943, 0.39885, 0.34829, 0.29775, 0.24726, 0.19683, 0.14651, 0.09636, 0.0714, 0.04656, 0.02194, 0.00981, 0.00509, 0.0028, 0.0, 0.0072, 0.00991, 0.01519, 0.02806, 0.05344, 0.0786, 0.10364, 0.15349, 0.20317, 0.25274, 0.30225, 0.35171, 0.40115, 0.45057, 0.5, 0.54944, 0.59893, 0.64851, 0.69822, 0.74809, 0.79815, 0.84838, 0.8988, 0.9494, 1.0]</Xcoordinate>
//                    <Zcoordinate from="input" unit="" varName="_zCoords">[0.0, 0.01275, 0.02744, 0.04276, 0.05794, 0.07238, 0.08539, 0.09639, 0.10464, 0.10923, 0.11148, 0.11188, 0.11059, 0.10759, 0.10287, 0.09633, 0.08773, 0.07669, 0.06231, 0.05347, 0.04306, 0.03, 0.02147, 0.01692, 0.01405, 0.0, -0.01205, -0.01412, -0.01719, -0.02256, -0.03042, -0.03651, -0.04163, -0.04977, -0.05589, -0.06053, -0.06399, -0.06639, -0.06775, -0.06808, -0.06736, -0.06543, -0.0618, -0.05519, -0.04651, -0.03658, -0.0261, -0.01584, -0.00676, -1.1E-4, 0.0]</Zcoordinate>
//                </Airfoil_Geometry>
//                <Airfoil_Aerodynamics id="10991099" level="4">
//                    <Alpha_zero_lift from="input" unit="°" varName="_alphaZeroLift">-1.5000</Alpha_zero_lift>
//                    <Alpha_end_linear from="input" unit="°" varName="_alphaStar">11.4592</Alpha_end_linear>
//                    <Alpha_stall from="input" unit="°" varName="_alphaStall">11.4592</Alpha_stall>
//                    <Cl_alpha from="input" unit="" varName="_clAlpha">6.1000</Cl_alpha>
//                    <Cd_min from="input" unit="" varName="_cdMin">0.000000</Cd_min>
//                    <Cl_at_Cdmin from="input" unit="" varName="_clAtCdMin">0.2000</Cl_at_Cdmin>
//                    <Cl_end_linear from="input" unit="" varName="_clStar">0.8000</Cl_end_linear>
//                    <Cl_max from="input" unit="" varName="_clMax">1.3000</Cl_max>
//                    <K_factor_drag from="input" unit="" varName="_kFactorDragPolar">0.1000</K_factor_drag>
//                    <Cm_alpha from="input" unit="" varName="_cmAlphaAC">0.000000</Cm_alpha>
//                    <Xac from="input" unit="" varName="_aerodynamicCenterX">0.3000</Xac>
//                    <CmAC from="input" unit="" varName="_cmAC">-0.070000</CmAC>
//                    <CmAC_at_stall from="input" unit="" varName="_cmACStall">-0.090000</CmAC_at_stall>
//                </Airfoil_Aerodynamics>
//            </Airfoil_1>
//            <Airfoil_2 id="1199" level="3">
//                <Family from="input" unit="" varName="_family">NACA63_209</Family>
//                <Type from="input" unit="" varName="_type">SUPERCRITICAL</Type>
//                <Airfoil_Geometry id="11990199" level="4">
//                    <Position_along_semispan unit="">4.2037</Position_along_semispan>
//                    <Twist_relative_to_root from="input" unit="°" varName="_twist">-1.5000</Twist_relative_to_root>
//                    <Thickness_to_chord_ratio_max from="input" unit="" varName="_maximumThicknessOverChord">0.1100</Thickness_to_chord_ratio_max>
//                    <Xcoordinate from="input" unit="" varName="_xCoords">[1.0, 0.9506, 0.9012, 0.85162, 0.80185, 0.75191, 0.70178, 0.65149, 0.60107, 0.55056, 0.5, 0.44943, 0.39885, 0.34829, 0.29775, 0.24726, 0.19683, 0.14651, 0.09636, 0.0714, 0.04656, 0.02194, 0.00981, 0.00509, 0.0028, 0.0, 0.0072, 0.00991, 0.01519, 0.02806, 0.05344, 0.0786, 0.10364, 0.15349, 0.20317, 0.25274, 0.30225, 0.35171, 0.40115, 0.45057, 0.5, 0.54944, 0.59893, 0.64851, 0.69822, 0.74809, 0.79815, 0.84838, 0.8988, 0.9494, 1.0]</Xcoordinate>
//                    <Zcoordinate from="input" unit="" varName="_zCoords">[0.0, 0.01275, 0.02744, 0.04276, 0.05794, 0.07238, 0.08539, 0.09639, 0.10464, 0.10923, 0.11148, 0.11188, 0.11059, 0.10759, 0.10287, 0.09633, 0.08773, 0.07669, 0.06231, 0.05347, 0.04306, 0.03, 0.02147, 0.01692, 0.01405, 0.0, -0.01205, -0.01412, -0.01719, -0.02256, -0.03042, -0.03651, -0.04163, -0.04977, -0.05589, -0.06053, -0.06399, -0.06639, -0.06775, -0.06808, -0.06736, -0.06543, -0.0618, -0.05519, -0.04651, -0.03658, -0.0261, -0.01584, -0.00676, -1.1E-4, 0.0]</Zcoordinate>
//                </Airfoil_Geometry>
//                <Airfoil_Aerodynamics id="11991199" level="4">
//                    <Alpha_zero_lift from="input" unit="°" varName="_alphaZeroLift">-2.5000</Alpha_zero_lift>
//                    <Alpha_end_linear from="input" unit="°" varName="_alphaStar">11.4592</Alpha_end_linear>
//                    <Alpha_stall from="input" unit="°" varName="_alphaStall">11.4592</Alpha_stall>
//                    <Cl_alpha from="input" unit="" varName="_clAlpha">6.1000</Cl_alpha>
//                    <Cd_min from="input" unit="" varName="_cdMin">0.000000</Cd_min>
//                    <Cl_at_Cdmin from="input" unit="" varName="_clAtCdMin">0.2000</Cl_at_Cdmin>
//                    <Cl_end_linear from="input" unit="" varName="_clStar">0.8000</Cl_end_linear>
//                    <Cl_max from="input" unit="" varName="_clMax">1.3000</Cl_max>
//                    <K_factor_drag from="input" unit="" varName="_kFactorDragPolar">0.1000</K_factor_drag>
//                    <Cm_alpha from="input" unit="" varName="_cmAlphaAC">0.000000</Cm_alpha>
//                    <Xac from="input" unit="" varName="_aerodynamicCenterX">0.3000</Xac>
//                    <CmAC from="input" unit="" varName="_cmAC">-0.070000</CmAC>
//                    <CmAC_at_stall from="input" unit="" varName="_cmACStall">-0.090000</CmAC_at_stall>
//                </Airfoil_Aerodynamics>
//            </Airfoil_2>
//            <Airfoil_3 id="1299" level="3">
//                <Family from="input" unit="" varName="_family">NACA63_209</Family>
//                <Type from="input" unit="" varName="_type">SUPERCRITICAL</Type>
//                <Airfoil_Geometry id="12990299" level="4">
//                    <Position_along_semispan unit="">14.0125</Position_along_semispan>
//                    <Twist_relative_to_root from="input" unit="°" varName="_twist">-4.5000</Twist_relative_to_root>
//                    <Thickness_to_chord_ratio_max from="input" unit="" varName="_maximumThicknessOverChord">0.0900</Thickness_to_chord_ratio_max>
//                    <Xcoordinate from="input" unit="" varName="_xCoords">[1.0, 0.9506, 0.9012, 0.85162, 0.80185, 0.75191, 0.70178, 0.65149, 0.60107, 0.55056, 0.5, 0.44943, 0.39885, 0.34829, 0.29775, 0.24726, 0.19683, 0.14651, 0.09636, 0.0714, 0.04656, 0.02194, 0.00981, 0.00509, 0.0028, 0.0, 0.0072, 0.00991, 0.01519, 0.02806, 0.05344, 0.0786, 0.10364, 0.15349, 0.20317, 0.25274, 0.30225, 0.35171, 0.40115, 0.45057, 0.5, 0.54944, 0.59893, 0.64851, 0.69822, 0.74809, 0.79815, 0.84838, 0.8988, 0.9494, 1.0]</Xcoordinate>
//                    <Zcoordinate from="input" unit="" varName="_zCoords">[0.0, 0.01275, 0.02744, 0.04276, 0.05794, 0.07238, 0.08539, 0.09639, 0.10464, 0.10923, 0.11148, 0.11188, 0.11059, 0.10759, 0.10287, 0.09633, 0.08773, 0.07669, 0.06231, 0.05347, 0.04306, 0.03, 0.02147, 0.01692, 0.01405, 0.0, -0.01205, -0.01412, -0.01719, -0.02256, -0.03042, -0.03651, -0.04163, -0.04977, -0.05589, -0.06053, -0.06399, -0.06639, -0.06775, -0.06808, -0.06736, -0.06543, -0.0618, -0.05519, -0.04651, -0.03658, -0.0261, -0.01584, -0.00676, -1.1E-4, 0.0]</Zcoordinate>
//                </Airfoil_Geometry>
//                <Airfoil_Aerodynamics id="12991299" level="4">
//                    <Alpha_zero_lift from="input" unit="°" varName="_alphaZeroLift">-3.0000</Alpha_zero_lift>
//                    <Alpha_end_linear from="input" unit="°" varName="_alphaStar">11.4592</Alpha_end_linear>
//                    <Alpha_stall from="input" unit="°" varName="_alphaStall">11.4592</Alpha_stall>
//                    <Cl_alpha from="input" unit="" varName="_clAlpha">6.1000</Cl_alpha>
//                    <Cd_min from="input" unit="" varName="_cdMin">0.000000</Cd_min>
//                    <Cl_at_Cdmin from="input" unit="" varName="_clAtCdMin">0.2000</Cl_at_Cdmin>
//                    <Cl_end_linear from="input" unit="" varName="_clStar">0.8000</Cl_end_linear>
//                    <Cl_max from="input" unit="" varName="_clMax">1.3000</Cl_max>
//                    <K_factor_drag from="input" unit="" varName="_kFactorDragPolar">0.1000</K_factor_drag>
//                    <Cm_alpha from="input" unit="" varName="_cmAlphaAC">0.000000</Cm_alpha>
//                    <Xac from="input" unit="" varName="_aerodynamicCenterX">0.3000</Xac>
//                    <CmAC from="input" unit="" varName="_cmAC">-0.070000</CmAC>
//                    <CmAC_at_stall from="input" unit="" varName="_cmACStall">-0.090000</CmAC_at_stall>
//                </Airfoil_Aerodynamics>
//            </Airfoil_3>
//        </Actual_Wing_parameters>
//    </Wing>
//    <HTail id="2">
//        <Equivalent_HTail_parameters>
//            <Xcoordinate from="input" unit="m" varName="_X0">29.4000</Xcoordinate>
//            <Ycoordinate from="input" unit="m" varName="_Y0">0.0000</Ycoordinate>
//            <Zcoordinate from="input" unit="m" varName="_Z0">4.4000</Zcoordinate>
//            <Planform_surface from="input" unit="m²" varName="_surface">21.7000</Planform_surface>
//            <Control_surface_extension from="input" unit="m²" varName="_surfaceCS">5.4000</Control_surface_extension>
//            <aspectRatio from="input" unit="" varName="_aspectRatio">4.6000</aspectRatio>
//            <taperRatio from="input" unit="" varName="_taperRatioEquivalent">0.4000</taperRatio>
//            <Wing_position_in_percent_of_fuselage_height from="input" unit="" varName="_positionRelativeToAttachment">1.0000</Wing_position_in_percent_of_fuselage_height>
//            <kinkSpanStation from="input" unit="" varName="_spanStationKink">1.0000</kinkSpanStation>
//            <Thickness_to_chord_ratio_root from="input" unit="" varName="_tc_root">0.1000</Thickness_to_chord_ratio_root>
//            <Thickness_to_chord_ratio_kink from="input" unit="" varName="_tc_kink">0.1000</Thickness_to_chord_ratio_kink>
//            <Thickness_to_chord_ratio_tip from="input" unit="" varName="_tc_tip">0.1000</Thickness_to_chord_ratio_tip>
//            <Root_chord_LE_extension from="input" unit="" varName="_extensionLERootChordLinPanel">0.000000</Root_chord_LE_extension>
//            <Root_chord_TE_extension from="input" unit="" varName="_extensionTERootChordLinPanel">0.000000</Root_chord_TE_extension>
//            <sweepc4 from="input" unit="°" varName="_sweepQuarterChordEq">28.6479</sweepc4>
//            <Incidence_relative_to_xBRF from="input" unit="°" varName="_iw">0.0000</Incidence_relative_to_xBRF>
//            <kinkStationTwist from="input" unit="°" varName="_twistKink">0.0000</kinkStationTwist>
//            <tipStationTwist from="input" unit="°" varName="_twistTip">0.0000</tipStationTwist>
//            <Dihedral_inner_panel from="input" unit="°" varName="_dihedralInnerPanel">5.7296</Dihedral_inner_panel>
//            <Dihedral_outer_panel from="input" unit="°" varName="_dihedralOuterPanel">0.0000</Dihedral_outer_panel>
//            <Surface_roughness from="input" unit="m" varName="_roughness">0.0000</Surface_roughness>
//            <Transition_point_in_percent_of_chord_upper_wing from="input" unit="" varName="_xTransitionU">0.1000</Transition_point_in_percent_of_chord_upper_wing>
//            <Transition_point_in_percent_of_chord_upper_wing from="input" unit="" varName="_xTransitionL">0.1200</Transition_point_in_percent_of_chord_upper_wing>
//            <Reference_mass from="input" unit="kg" varName="_massReference">491.0000</Reference_mass>
//            <Composite_correction_factor from="input" unit="">not_initialized</Composite_correction_factor>
//            <Mass_correction_factor from="input" unit="">not_initialized</Mass_correction_factor>
//            <sweepLE unit="°">32.5977</sweepLE>
//            <Mean_aerodynamic_chord_MAC unit="m">2.3049</Mean_aerodynamic_chord_MAC>
//            <Root_chord unit="m">3.1028</Root_chord>
//        </Equivalent_HTail_parameters>
//        <Actual_HTail_parameters>
//            <Mach_number_transonic_threshold from="input" unit="" varName="_machTransonicThreshold">0.7000</Mach_number_transonic_threshold>
//            <Planform_surface unit="m²">21.7000</Planform_surface>
//            <Exposed_surface unit="m²">16.1602</Exposed_surface>
//            <Wetted_surface unit="m²">44.2680</Wetted_surface>
//            <Exposed_Wetted_surface unit="m²">32.9669</Exposed_Wetted_surface>
//            <span unit="m">9.9910</span>
//            <taperRatio unit="">0.4000</taperRatio>
//            <semiSurfaceInnerPanel unit="m²">10.8500</semiSurfaceInnerPanel>
//            <aspectRatioInnerPanel unit="">4.6000</aspectRatioInnerPanel>
//            <semiSurfaceOuterPanel unit="m²">0.0000</semiSurfaceOuterPanel>
//            <aspectRatioOuterPanel unit="">4.6000</aspectRatioOuterPanel>
//            <taperRatioInnerPanel unit="">0.4000</taperRatioInnerPanel>
//            <taperRatioOuterPanel unit="">1.0000</taperRatioOuterPanel>
//            <sweepLEInnerPanel unit="°">32.5977</sweepLEInnerPanel>
//            <sweepc4InnerPanel unit="°">28.6479</sweepc4InnerPanel>
//            <sweepLEOuterPanel unit="°">32.5977</sweepLEOuterPanel>
//            <sweepc4OuterPanel unit="°">28.6479</sweepc4OuterPanel>
//            <rootChord unit="m">3.1028</rootChord>
//            <rootChordXle unit="m">0.0000</rootChordXle>
//            <kinkChord unit="m">1.2411</kinkChord>
//            <kinkChordXle unit="m">3.1945</kinkChordXle>
//            <tipChord unit="m">1.2411</tipChord>
//            <tipChordXle unit="m">3.1945</tipChordXle>
//            <Mean_aerodynamic_chord_MAC unit="m">2.3052</Mean_aerodynamic_chord_MAC>
//            <Mean_aerodynamic_chord_xLE_LRF unit="m">1.3685</Mean_aerodynamic_chord_xLE_LRF>
//            <Mean_aerodynamic_chord_yLE_LRF unit="m">2.1401</Mean_aerodynamic_chord_yLE_LRF>
//            <Mean_aerodynamic_chord_xLE_BRF unit="m">30.7685</Mean_aerodynamic_chord_xLE_BRF>
//            <Mean_aerodynamic_chord_yLE_BRF unit="m">2.1401</Mean_aerodynamic_chord_yLE_BRF>
//            <Mean_dihedral_angle unit="°">5.7296</Mean_dihedral_angle>
//            <AC_to_CG_distance unit="m">17.6910</AC_to_CG_distance>
//            <AC_to_Wing_AC_distance unit="m">14.7872</AC_to_Wing_AC_distance>
//            <Volumetric_ratio unit="">0.8829</Volumetric_ratio>
//            <Mean_maximum_thickness>
//                <Integral_mean unit="">0.1500</Integral_mean>
//            </Mean_maximum_thickness>
//            <Number_of_airfoils unit="">2</Number_of_airfoils>
//            <Airfoil_1 id="2099" level="3">
//                <Family from="input" unit="" varName="_family">NACA63_209</Family>
//                <Type from="input" unit="" varName="_type">SUPERCRITICAL</Type>
//                <Airfoil_Geometry id="20990099" level="4">
//                    <Position_along_semispan unit="">0.000000</Position_along_semispan>
//                    <Twist_relative_to_root from="input" unit="°" varName="_twist">0.0000</Twist_relative_to_root>
//                    <Thickness_to_chord_ratio_max from="input" unit="" varName="_maximumThicknessOverChord">0.1500</Thickness_to_chord_ratio_max>
//                    <Xcoordinate from="input" unit="" varName="_xCoords">[1.0, 0.99372, 0.982766, 0.969978, 0.955648, 0.940249, 0.924212, 0.907834, 0.89128, 0.874637, 0.857947, 0.84123, 0.824498, 0.807755, 0.791005, 0.77425, 0.757492, 0.740731, 0.72397, 0.707209, 0.690451, 0.673695, 0.656944, 0.640199, 0.62346, 0.606731, 0.590011, 0.573303, 0.556607, 0.539927, 0.523262, 0.506616, 0.48999, 0.473385, 0.456805, 0.440251, 0.423726, 0.407233, 0.390775, 0.374354, 0.357976, 0.341643, 0.325361, 0.309135, 0.292972, 0.276878, 0.260863, 0.244936, 0.22911, 0.213401, 0.197828, 0.182416, 0.167201, 0.152227, 0.137561, 0.123289, 0.109532, 0.096442, 0.084194, 0.072956, 0.06285, 0.053918, 0.046123, 0.039362, 0.033509, 0.028432, 0.024011, 0.020146, 0.016754, 0.013769, 0.011142, 0.008834, 0.006819, 0.005076, 0.003597, 0.002376, 0.001412, 7.03E-4, 2.45E-4, 2.6E-5, 2.6E-5, 2.45E-4, 7.03E-4, 0.001412, 0.002376, 0.003597, 0.005076, 0.006819, 0.008834, 0.011142, 0.013769, 0.016754, 0.020146, 0.024011, 0.028432, 0.033509, 0.039363, 0.046123, 0.053919, 0.06285, 0.072956, 0.084194, 0.096442, 0.109532, 0.123289, 0.137561, 0.152228, 0.167201, 0.182417, 0.197828, 0.213401, 0.22911, 0.244936, 0.260863, 0.276879, 0.292972, 0.309136, 0.325361, 0.341643, 0.357976, 0.374355, 0.390775, 0.407233, 0.423727, 0.440251, 0.456805, 0.473386, 0.48999, 0.506616, 0.523263, 0.539927, 0.556608, 0.573303, 0.590011, 0.606731, 0.623461, 0.640199, 0.656944, 0.673695, 0.690451, 0.70721, 0.72397, 0.740731, 0.757492, 0.77425, 0.791005, 0.807755, 0.824498, 0.84123, 0.857947, 0.874637, 0.89128, 0.907834, 0.924213, 0.940249, 0.955649, 0.969978, 0.982766, 0.99372, 1.0]</Xcoordinate>
//                    <Zcoordinate from="input" unit="" varName="_zCoords">[0.00126, 0.002138, 0.003653, 0.005396, 0.007317, 0.009346, 0.011419, 0.013497, 0.015557, 0.017589, 0.019588, 0.021551, 0.023478, 0.025368, 0.027222, 0.029039, 0.030819, 0.032562, 0.034267, 0.035934, 0.037562, 0.039149, 0.040696, 0.0422, 0.043661, 0.045077, 0.046446, 0.047766, 0.049036, 0.050253, 0.051414, 0.052518, 0.053561, 0.05454, 0.055453, 0.056295, 0.057063, 0.057753, 0.058361, 0.058882, 0.059311, 0.059644, 0.059875, 0.059998, 0.060007, 0.059894, 0.059654, 0.059277, 0.058757, 0.058084, 0.057248, 0.056241, 0.055054, 0.053676, 0.052101, 0.050328, 0.04836, 0.046216, 0.043928, 0.041542, 0.039116, 0.036701, 0.034339, 0.032051, 0.029846, 0.027721, 0.025669, 0.023677, 0.021733, 0.019823, 0.017936, 0.016059, 0.014185, 0.012303, 0.010409, 0.008502, 0.006586, 0.00467, 0.002769, 9.09E-4, -9.09E-4, -0.002769, -0.00467, -0.006586, -0.008502, -0.010409, -0.012303, -0.014185, -0.016059, -0.017936, -0.019823, -0.021733, -0.023677, -0.025669, -0.027721, -0.029846, -0.032051, -0.034339, -0.036701, -0.039116, -0.041542, -0.043928, -0.046216, -0.04836, -0.050328, -0.052101, -0.053676, -0.055054, -0.056241, -0.057248, -0.058084, -0.058757, -0.059277, -0.059654, -0.059894, -0.060007, -0.059998, -0.059875, -0.059644, -0.059311, -0.058882, -0.058361, -0.057753, -0.057063, -0.056295, -0.055453, -0.05454, -0.053561, -0.052518, -0.051414, -0.050253, -0.049036, -0.047766, -0.046446, -0.045077, -0.043661, -0.0422, -0.040696, -0.039149, -0.037562, -0.035934, -0.034267, -0.032562, -0.030819, -0.029039, -0.027222, -0.025368, -0.023478, -0.021551, -0.019588, -0.017589, -0.015557, -0.013497, -0.011419, -0.009346, -0.007317, -0.005396, -0.003653, -0.002138, -0.00126]</Zcoordinate>
//                </Airfoil_Geometry>
//                <Airfoil_Aerodynamics id="20991099" level="4">
//                    <Alpha_zero_lift from="input" unit="°" varName="_alphaZeroLift">-2.5000</Alpha_zero_lift>
//                    <Alpha_end_linear from="input" unit="°" varName="_alphaStar">10.0000</Alpha_end_linear>
//                    <Alpha_stall from="input" unit="°" varName="_alphaStall">12.0000</Alpha_stall>
//                    <Cl_alpha from="input" unit="" varName="_clAlpha">6.1000</Cl_alpha>
//                    <Cd_min from="input" unit="" varName="_cdMin">0.0250</Cd_min>
//                    <Cl_at_Cdmin from="input" unit="" varName="_clAtCdMin">0.2000</Cl_at_Cdmin>
//                    <Cl_end_linear from="input" unit="" varName="_clStar">0.8000</Cl_end_linear>
//                    <Cl_max from="input" unit="" varName="_clMax">1.3000</Cl_max>
//                    <K_factor_drag from="input" unit="" varName="_kFactorDragPolar">0.0750</K_factor_drag>
//                    <Cm_alpha from="input" unit="" varName="_cmAlphaAC">0.000000</Cm_alpha>
//                    <Xac from="input" unit="" varName="_aerodynamicCenterX">0.2500</Xac>
//                    <CmAC from="input" unit="" varName="_cmAC">-0.070000</CmAC>
//                    <CmAC_at_stall from="input" unit="" varName="_cmACStall">-0.090000</CmAC_at_stall>
//                </Airfoil_Aerodynamics>
//            </Airfoil_1>
//            <Airfoil_2 id="2299" level="3">
//                <Family from="input" unit="" varName="_family">NACA63_209</Family>
//                <Type from="input" unit="" varName="_type">SUPERCRITICAL</Type>
//                <Airfoil_Geometry id="22990299" level="4">
//                    <Position_along_semispan unit="">4.9955</Position_along_semispan>
//                    <Twist_relative_to_root from="input" unit="°" varName="_twist">0.0000</Twist_relative_to_root>
//                    <Thickness_to_chord_ratio_max from="input" unit="" varName="_maximumThicknessOverChord">0.1500</Thickness_to_chord_ratio_max>
//                    <Xcoordinate from="input" unit="" varName="_xCoords">[1.0, 0.99372, 0.982766, 0.969978, 0.955648, 0.940249, 0.924212, 0.907834, 0.89128, 0.874637, 0.857947, 0.84123, 0.824498, 0.807755, 0.791005, 0.77425, 0.757492, 0.740731, 0.72397, 0.707209, 0.690451, 0.673695, 0.656944, 0.640199, 0.62346, 0.606731, 0.590011, 0.573303, 0.556607, 0.539927, 0.523262, 0.506616, 0.48999, 0.473385, 0.456805, 0.440251, 0.423726, 0.407233, 0.390775, 0.374354, 0.357976, 0.341643, 0.325361, 0.309135, 0.292972, 0.276878, 0.260863, 0.244936, 0.22911, 0.213401, 0.197828, 0.182416, 0.167201, 0.152227, 0.137561, 0.123289, 0.109532, 0.096442, 0.084194, 0.072956, 0.06285, 0.053918, 0.046123, 0.039362, 0.033509, 0.028432, 0.024011, 0.020146, 0.016754, 0.013769, 0.011142, 0.008834, 0.006819, 0.005076, 0.003597, 0.002376, 0.001412, 7.03E-4, 2.45E-4, 2.6E-5, 2.6E-5, 2.45E-4, 7.03E-4, 0.001412, 0.002376, 0.003597, 0.005076, 0.006819, 0.008834, 0.011142, 0.013769, 0.016754, 0.020146, 0.024011, 0.028432, 0.033509, 0.039363, 0.046123, 0.053919, 0.06285, 0.072956, 0.084194, 0.096442, 0.109532, 0.123289, 0.137561, 0.152228, 0.167201, 0.182417, 0.197828, 0.213401, 0.22911, 0.244936, 0.260863, 0.276879, 0.292972, 0.309136, 0.325361, 0.341643, 0.357976, 0.374355, 0.390775, 0.407233, 0.423727, 0.440251, 0.456805, 0.473386, 0.48999, 0.506616, 0.523263, 0.539927, 0.556608, 0.573303, 0.590011, 0.606731, 0.623461, 0.640199, 0.656944, 0.673695, 0.690451, 0.70721, 0.72397, 0.740731, 0.757492, 0.77425, 0.791005, 0.807755, 0.824498, 0.84123, 0.857947, 0.874637, 0.89128, 0.907834, 0.924213, 0.940249, 0.955649, 0.969978, 0.982766, 0.99372, 1.0]</Xcoordinate>
//                    <Zcoordinate from="input" unit="" varName="_zCoords">[0.00126, 0.002138, 0.003653, 0.005396, 0.007317, 0.009346, 0.011419, 0.013497, 0.015557, 0.017589, 0.019588, 0.021551, 0.023478, 0.025368, 0.027222, 0.029039, 0.030819, 0.032562, 0.034267, 0.035934, 0.037562, 0.039149, 0.040696, 0.0422, 0.043661, 0.045077, 0.046446, 0.047766, 0.049036, 0.050253, 0.051414, 0.052518, 0.053561, 0.05454, 0.055453, 0.056295, 0.057063, 0.057753, 0.058361, 0.058882, 0.059311, 0.059644, 0.059875, 0.059998, 0.060007, 0.059894, 0.059654, 0.059277, 0.058757, 0.058084, 0.057248, 0.056241, 0.055054, 0.053676, 0.052101, 0.050328, 0.04836, 0.046216, 0.043928, 0.041542, 0.039116, 0.036701, 0.034339, 0.032051, 0.029846, 0.027721, 0.025669, 0.023677, 0.021733, 0.019823, 0.017936, 0.016059, 0.014185, 0.012303, 0.010409, 0.008502, 0.006586, 0.00467, 0.002769, 9.09E-4, -9.09E-4, -0.002769, -0.00467, -0.006586, -0.008502, -0.010409, -0.012303, -0.014185, -0.016059, -0.017936, -0.019823, -0.021733, -0.023677, -0.025669, -0.027721, -0.029846, -0.032051, -0.034339, -0.036701, -0.039116, -0.041542, -0.043928, -0.046216, -0.04836, -0.050328, -0.052101, -0.053676, -0.055054, -0.056241, -0.057248, -0.058084, -0.058757, -0.059277, -0.059654, -0.059894, -0.060007, -0.059998, -0.059875, -0.059644, -0.059311, -0.058882, -0.058361, -0.057753, -0.057063, -0.056295, -0.055453, -0.05454, -0.053561, -0.052518, -0.051414, -0.050253, -0.049036, -0.047766, -0.046446, -0.045077, -0.043661, -0.0422, -0.040696, -0.039149, -0.037562, -0.035934, -0.034267, -0.032562, -0.030819, -0.029039, -0.027222, -0.025368, -0.023478, -0.021551, -0.019588, -0.017589, -0.015557, -0.013497, -0.011419, -0.009346, -0.007317, -0.005396, -0.003653, -0.002138, -0.00126]</Zcoordinate>
//                </Airfoil_Geometry>
//                <Airfoil_Aerodynamics id="22991299" level="4">
//                    <Alpha_zero_lift from="input" unit="°" varName="_alphaZeroLift">-2.5000</Alpha_zero_lift>
//                    <Alpha_end_linear from="input" unit="°" varName="_alphaStar">10.0000</Alpha_end_linear>
//                    <Alpha_stall from="input" unit="°" varName="_alphaStall">12.0000</Alpha_stall>
//                    <Cl_alpha from="input" unit="" varName="_clAlpha">6.1000</Cl_alpha>
//                    <Cd_min from="input" unit="" varName="_cdMin">0.0250</Cd_min>
//                    <Cl_at_Cdmin from="input" unit="" varName="_clAtCdMin">0.2000</Cl_at_Cdmin>
//                    <Cl_end_linear from="input" unit="" varName="_clStar">0.8000</Cl_end_linear>
//                    <Cl_max from="input" unit="" varName="_clMax">1.3000</Cl_max>
//                    <K_factor_drag from="input" unit="" varName="_kFactorDragPolar">0.0750</K_factor_drag>
//                    <Cm_alpha from="input" unit="" varName="_cmAlphaAC">0.000000</Cm_alpha>
//                    <Xac from="input" unit="" varName="_aerodynamicCenterX">0.2500</Xac>
//                    <CmAC from="input" unit="" varName="_cmAC">-0.070000</CmAC>
//                    <CmAC_at_stall from="input" unit="" varName="_cmACStall">-0.090000</CmAC_at_stall>
//                </Airfoil_Aerodynamics>
//            </Airfoil_2>
//        </Actual_HTail_parameters>
//    </HTail>
//    <VTail id="3">
//        <Equivalent_VTail_parameters>
//            <Xcoordinate from="input" unit="m" varName="_X0">27.5000</Xcoordinate>
//            <Ycoordinate from="input" unit="m" varName="_Y0">0.0000</Ycoordinate>
//            <Zcoordinate from="input" unit="m" varName="_Z0">1.4000</Zcoordinate>
//            <Planform_surface from="input" unit="m²" varName="_surface">12.3000</Planform_surface>
//            <Control_surface_extension from="input" unit="m²" varName="_surfaceCS">3.1000</Control_surface_extension>
//            <aspectRatio from="input" unit="" varName="_aspectRatio">0.9000</aspectRatio>
//            <taperRatio from="input" unit="" varName="_taperRatioEquivalent">0.7000</taperRatio>
//            <Wing_position_in_percent_of_fuselage_height from="input" unit="" varName="_positionRelativeToAttachment">1.0000</Wing_position_in_percent_of_fuselage_height>
//            <kinkSpanStation from="input" unit="" varName="_spanStationKink">1.0000</kinkSpanStation>
//            <Thickness_to_chord_ratio_root from="input" unit="" varName="_tc_root">0.1000</Thickness_to_chord_ratio_root>
//            <Thickness_to_chord_ratio_kink from="input" unit="" varName="_tc_kink">0.1000</Thickness_to_chord_ratio_kink>
//            <Thickness_to_chord_ratio_tip from="input" unit="" varName="_tc_tip">0.1000</Thickness_to_chord_ratio_tip>
//            <Root_chord_LE_extension from="input" unit="" varName="_extensionLERootChordLinPanel">0.000000</Root_chord_LE_extension>
//            <Root_chord_TE_extension from="input" unit="" varName="_extensionTERootChordLinPanel">0.000000</Root_chord_TE_extension>
//            <sweepc4 from="input" unit="°" varName="_sweepQuarterChordEq">40.1071</sweepc4>
//            <Incidence_relative_to_xBRF from="input" unit="°" varName="_iw">0.0000</Incidence_relative_to_xBRF>
//            <kinkStationTwist from="input" unit="°" varName="_twistKink">0.0000</kinkStationTwist>
//            <tipStationTwist from="input" unit="°" varName="_twistTip">0.0000</tipStationTwist>
//            <Dihedral_inner_panel from="input" unit="°" varName="_dihedralInnerPanel">0.0000</Dihedral_inner_panel>
//            <Dihedral_outer_panel from="input" unit="°" varName="_dihedralOuterPanel">0.0000</Dihedral_outer_panel>
//            <Surface_roughness from="input" unit="m" varName="_roughness">0.0000</Surface_roughness>
//            <Transition_point_in_percent_of_chord_upper_wing from="input" unit="" varName="_xTransitionU">0.1000</Transition_point_in_percent_of_chord_upper_wing>
//            <Transition_point_in_percent_of_chord_upper_wing from="input" unit="" varName="_xTransitionL">0.1200</Transition_point_in_percent_of_chord_upper_wing>
//            <Reference_mass from="input" unit="kg" varName="_massReference">365.0000</Reference_mass>
//            <Composite_correction_factor from="input" unit="">not_initialized</Composite_correction_factor>
//            <Mass_correction_factor from="input" unit="">not_initialized</Mass_correction_factor>
//            <sweepLE unit="°">46.0784</sweepLE>
//            <Mean_aerodynamic_chord_MAC unit="m">3.7352</Mean_aerodynamic_chord_MAC>
//            <Root_chord unit="m">4.3492</Root_chord>
//        </Equivalent_VTail_parameters>
//        <Actual_VTail_parameters>
//            <Mach_number_transonic_threshold from="input" unit="" varName="_machTransonicThreshold">0.7000</Mach_number_transonic_threshold>
//            <Planform_surface unit="m²">12.3000</Planform_surface>
//            <Exposed_surface unit="m²">2.6409</Exposed_surface>
//            <Wetted_surface unit="m²">25.0920</Wetted_surface>
//            <Exposed_Wetted_surface unit="m²">5.3874</Exposed_Wetted_surface>
//            <span unit="m">3.3272</span>
//            <taperRatio unit="">0.7000</taperRatio>
//            <semiSurfaceInnerPanel unit="m²">6.1500</semiSurfaceInnerPanel>
//            <aspectRatioInnerPanel unit="">0.9000</aspectRatioInnerPanel>
//            <semiSurfaceOuterPanel unit="m²">0.0000</semiSurfaceOuterPanel>
//            <aspectRatioOuterPanel unit="">0.9000</aspectRatioOuterPanel>
//            <taperRatioInnerPanel unit="">0.7000</taperRatioInnerPanel>
//            <taperRatioOuterPanel unit="">1.0000</taperRatioOuterPanel>
//            <sweepLEInnerPanel unit="°">46.0784</sweepLEInnerPanel>
//            <sweepc4InnerPanel unit="°">40.1071</sweepc4InnerPanel>
//            <sweepLEOuterPanel unit="°">46.0784</sweepLEOuterPanel>
//            <sweepc4OuterPanel unit="°">40.1071</sweepc4OuterPanel>
//            <rootChord unit="m">4.3492</rootChord>
//            <rootChordXle unit="m">0.0000</rootChordXle>
//            <kinkChord unit="m">3.0445</kinkChord>
//            <kinkChordXle unit="m">1.7274</kinkChordXle>
//            <tipChord unit="m">3.0445</tipChord>
//            <tipChordXle unit="m">1.7274</tipChordXle>
//            <Mean_aerodynamic_chord_MAC unit="m">3.7353</Mean_aerodynamic_chord_MAC>
//            <Mean_aerodynamic_chord_xLE_LRF unit="m">0.8128</Mean_aerodynamic_chord_xLE_LRF>
//            <Mean_aerodynamic_chord_yLE_LRF unit="m">0.7827</Mean_aerodynamic_chord_yLE_LRF>
//            <Mean_aerodynamic_chord_xLE_BRF unit="m">28.3128</Mean_aerodynamic_chord_xLE_BRF>
//            <Mean_aerodynamic_chord_yLE_BRF unit="m">0.7827</Mean_aerodynamic_chord_yLE_BRF>
//            <Mean_dihedral_angle unit="°">0.0000</Mean_dihedral_angle>
//            <AC_to_CG_distance unit="m">15.5928</AC_to_CG_distance>
//            <AC_to_Wing_AC_distance unit="m">12.6890</AC_to_Wing_AC_distance>
//            <Volumetric_ratio unit="">0.0596</Volumetric_ratio>
//            <Mean_maximum_thickness>
//                <Integral_mean unit="">0.2735</Integral_mean>
//            </Mean_maximum_thickness>
//            <Number_of_airfoils unit="">2</Number_of_airfoils>
//            <Airfoil_1 id="3099" level="3">
//                <Family from="input" unit="" varName="_family">NACA63_209</Family>
//                <Type from="input" unit="" varName="_type">SUPERCRITICAL</Type>
//                <Airfoil_Geometry id="30990099" level="4">
//                    <Position_along_semispan unit="">0.000000</Position_along_semispan>
//                    <Twist_relative_to_root from="input" unit="°" varName="_twist">0.0000</Twist_relative_to_root>
//                    <Thickness_to_chord_ratio_max from="input" unit="" varName="_maximumThicknessOverChord">0.1500</Thickness_to_chord_ratio_max>
//                    <Xcoordinate from="input" unit="" varName="_xCoords">[1.0, 0.99372, 0.982766, 0.969978, 0.955648, 0.940249, 0.924212, 0.907834, 0.89128, 0.874637, 0.857947, 0.84123, 0.824498, 0.807755, 0.791005, 0.77425, 0.757492, 0.740731, 0.72397, 0.707209, 0.690451, 0.673695, 0.656944, 0.640199, 0.62346, 0.606731, 0.590011, 0.573303, 0.556607, 0.539927, 0.523262, 0.506616, 0.48999, 0.473385, 0.456805, 0.440251, 0.423726, 0.407233, 0.390775, 0.374354, 0.357976, 0.341643, 0.325361, 0.309135, 0.292972, 0.276878, 0.260863, 0.244936, 0.22911, 0.213401, 0.197828, 0.182416, 0.167201, 0.152227, 0.137561, 0.123289, 0.109532, 0.096442, 0.084194, 0.072956, 0.06285, 0.053918, 0.046123, 0.039362, 0.033509, 0.028432, 0.024011, 0.020146, 0.016754, 0.013769, 0.011142, 0.008834, 0.006819, 0.005076, 0.003597, 0.002376, 0.001412, 7.03E-4, 2.45E-4, 2.6E-5, 2.6E-5, 2.45E-4, 7.03E-4, 0.001412, 0.002376, 0.003597, 0.005076, 0.006819, 0.008834, 0.011142, 0.013769, 0.016754, 0.020146, 0.024011, 0.028432, 0.033509, 0.039363, 0.046123, 0.053919, 0.06285, 0.072956, 0.084194, 0.096442, 0.109532, 0.123289, 0.137561, 0.152228, 0.167201, 0.182417, 0.197828, 0.213401, 0.22911, 0.244936, 0.260863, 0.276879, 0.292972, 0.309136, 0.325361, 0.341643, 0.357976, 0.374355, 0.390775, 0.407233, 0.423727, 0.440251, 0.456805, 0.473386, 0.48999, 0.506616, 0.523263, 0.539927, 0.556608, 0.573303, 0.590011, 0.606731, 0.623461, 0.640199, 0.656944, 0.673695, 0.690451, 0.70721, 0.72397, 0.740731, 0.757492, 0.77425, 0.791005, 0.807755, 0.824498, 0.84123, 0.857947, 0.874637, 0.89128, 0.907834, 0.924213, 0.940249, 0.955649, 0.969978, 0.982766, 0.99372, 1.0]</Xcoordinate>
//                    <Zcoordinate from="input" unit="" varName="_zCoords">[0.00126, 0.002138, 0.003653, 0.005396, 0.007317, 0.009346, 0.011419, 0.013497, 0.015557, 0.017589, 0.019588, 0.021551, 0.023478, 0.025368, 0.027222, 0.029039, 0.030819, 0.032562, 0.034267, 0.035934, 0.037562, 0.039149, 0.040696, 0.0422, 0.043661, 0.045077, 0.046446, 0.047766, 0.049036, 0.050253, 0.051414, 0.052518, 0.053561, 0.05454, 0.055453, 0.056295, 0.057063, 0.057753, 0.058361, 0.058882, 0.059311, 0.059644, 0.059875, 0.059998, 0.060007, 0.059894, 0.059654, 0.059277, 0.058757, 0.058084, 0.057248, 0.056241, 0.055054, 0.053676, 0.052101, 0.050328, 0.04836, 0.046216, 0.043928, 0.041542, 0.039116, 0.036701, 0.034339, 0.032051, 0.029846, 0.027721, 0.025669, 0.023677, 0.021733, 0.019823, 0.017936, 0.016059, 0.014185, 0.012303, 0.010409, 0.008502, 0.006586, 0.00467, 0.002769, 9.09E-4, -9.09E-4, -0.002769, -0.00467, -0.006586, -0.008502, -0.010409, -0.012303, -0.014185, -0.016059, -0.017936, -0.019823, -0.021733, -0.023677, -0.025669, -0.027721, -0.029846, -0.032051, -0.034339, -0.036701, -0.039116, -0.041542, -0.043928, -0.046216, -0.04836, -0.050328, -0.052101, -0.053676, -0.055054, -0.056241, -0.057248, -0.058084, -0.058757, -0.059277, -0.059654, -0.059894, -0.060007, -0.059998, -0.059875, -0.059644, -0.059311, -0.058882, -0.058361, -0.057753, -0.057063, -0.056295, -0.055453, -0.05454, -0.053561, -0.052518, -0.051414, -0.050253, -0.049036, -0.047766, -0.046446, -0.045077, -0.043661, -0.0422, -0.040696, -0.039149, -0.037562, -0.035934, -0.034267, -0.032562, -0.030819, -0.029039, -0.027222, -0.025368, -0.023478, -0.021551, -0.019588, -0.017589, -0.015557, -0.013497, -0.011419, -0.009346, -0.007317, -0.005396, -0.003653, -0.002138, -0.00126]</Zcoordinate>
//                </Airfoil_Geometry>
//                <Airfoil_Aerodynamics id="30991099" level="4">
//                    <Alpha_zero_lift from="input" unit="°" varName="_alphaZeroLift">-2.5000</Alpha_zero_lift>
//                    <Alpha_end_linear from="input" unit="°" varName="_alphaStar">10.0000</Alpha_end_linear>
//                    <Alpha_stall from="input" unit="°" varName="_alphaStall">12.0000</Alpha_stall>
//                    <Cl_alpha from="input" unit="" varName="_clAlpha">6.1000</Cl_alpha>
//                    <Cd_min from="input" unit="" varName="_cdMin">0.0250</Cd_min>
//                    <Cl_at_Cdmin from="input" unit="" varName="_clAtCdMin">0.2000</Cl_at_Cdmin>
//                    <Cl_end_linear from="input" unit="" varName="_clStar">0.8000</Cl_end_linear>
//                    <Cl_max from="input" unit="" varName="_clMax">1.3000</Cl_max>
//                    <K_factor_drag from="input" unit="" varName="_kFactorDragPolar">0.0750</K_factor_drag>
//                    <Cm_alpha from="input" unit="" varName="_cmAlphaAC">0.000000</Cm_alpha>
//                    <Xac from="input" unit="" varName="_aerodynamicCenterX">0.2500</Xac>
//                    <CmAC from="input" unit="" varName="_cmAC">-0.070000</CmAC>
//                    <CmAC_at_stall from="input" unit="" varName="_cmACStall">-0.090000</CmAC_at_stall>
//                </Airfoil_Aerodynamics>
//            </Airfoil_1>
//            <Airfoil_2 id="3299" level="3">
//                <Family from="input" unit="" varName="_family">NACA63_209</Family>
//                <Type from="input" unit="" varName="_type">SUPERCRITICAL</Type>
//                <Airfoil_Geometry id="32990299" level="4">
//                    <Position_along_semispan unit="">3.3272</Position_along_semispan>
//                    <Twist_relative_to_root from="input" unit="°" varName="_twist">0.0000</Twist_relative_to_root>
//                    <Thickness_to_chord_ratio_max from="input" unit="" varName="_maximumThicknessOverChord">0.1500</Thickness_to_chord_ratio_max>
//                    <Xcoordinate from="input" unit="" varName="_xCoords">[1.0, 0.99372, 0.982766, 0.969978, 0.955648, 0.940249, 0.924212, 0.907834, 0.89128, 0.874637, 0.857947, 0.84123, 0.824498, 0.807755, 0.791005, 0.77425, 0.757492, 0.740731, 0.72397, 0.707209, 0.690451, 0.673695, 0.656944, 0.640199, 0.62346, 0.606731, 0.590011, 0.573303, 0.556607, 0.539927, 0.523262, 0.506616, 0.48999, 0.473385, 0.456805, 0.440251, 0.423726, 0.407233, 0.390775, 0.374354, 0.357976, 0.341643, 0.325361, 0.309135, 0.292972, 0.276878, 0.260863, 0.244936, 0.22911, 0.213401, 0.197828, 0.182416, 0.167201, 0.152227, 0.137561, 0.123289, 0.109532, 0.096442, 0.084194, 0.072956, 0.06285, 0.053918, 0.046123, 0.039362, 0.033509, 0.028432, 0.024011, 0.020146, 0.016754, 0.013769, 0.011142, 0.008834, 0.006819, 0.005076, 0.003597, 0.002376, 0.001412, 7.03E-4, 2.45E-4, 2.6E-5, 2.6E-5, 2.45E-4, 7.03E-4, 0.001412, 0.002376, 0.003597, 0.005076, 0.006819, 0.008834, 0.011142, 0.013769, 0.016754, 0.020146, 0.024011, 0.028432, 0.033509, 0.039363, 0.046123, 0.053919, 0.06285, 0.072956, 0.084194, 0.096442, 0.109532, 0.123289, 0.137561, 0.152228, 0.167201, 0.182417, 0.197828, 0.213401, 0.22911, 0.244936, 0.260863, 0.276879, 0.292972, 0.309136, 0.325361, 0.341643, 0.357976, 0.374355, 0.390775, 0.407233, 0.423727, 0.440251, 0.456805, 0.473386, 0.48999, 0.506616, 0.523263, 0.539927, 0.556608, 0.573303, 0.590011, 0.606731, 0.623461, 0.640199, 0.656944, 0.673695, 0.690451, 0.70721, 0.72397, 0.740731, 0.757492, 0.77425, 0.791005, 0.807755, 0.824498, 0.84123, 0.857947, 0.874637, 0.89128, 0.907834, 0.924213, 0.940249, 0.955649, 0.969978, 0.982766, 0.99372, 1.0]</Xcoordinate>
//                    <Zcoordinate from="input" unit="" varName="_zCoords">[0.00126, 0.002138, 0.003653, 0.005396, 0.007317, 0.009346, 0.011419, 0.013497, 0.015557, 0.017589, 0.019588, 0.021551, 0.023478, 0.025368, 0.027222, 0.029039, 0.030819, 0.032562, 0.034267, 0.035934, 0.037562, 0.039149, 0.040696, 0.0422, 0.043661, 0.045077, 0.046446, 0.047766, 0.049036, 0.050253, 0.051414, 0.052518, 0.053561, 0.05454, 0.055453, 0.056295, 0.057063, 0.057753, 0.058361, 0.058882, 0.059311, 0.059644, 0.059875, 0.059998, 0.060007, 0.059894, 0.059654, 0.059277, 0.058757, 0.058084, 0.057248, 0.056241, 0.055054, 0.053676, 0.052101, 0.050328, 0.04836, 0.046216, 0.043928, 0.041542, 0.039116, 0.036701, 0.034339, 0.032051, 0.029846, 0.027721, 0.025669, 0.023677, 0.021733, 0.019823, 0.017936, 0.016059, 0.014185, 0.012303, 0.010409, 0.008502, 0.006586, 0.00467, 0.002769, 9.09E-4, -9.09E-4, -0.002769, -0.00467, -0.006586, -0.008502, -0.010409, -0.012303, -0.014185, -0.016059, -0.017936, -0.019823, -0.021733, -0.023677, -0.025669, -0.027721, -0.029846, -0.032051, -0.034339, -0.036701, -0.039116, -0.041542, -0.043928, -0.046216, -0.04836, -0.050328, -0.052101, -0.053676, -0.055054, -0.056241, -0.057248, -0.058084, -0.058757, -0.059277, -0.059654, -0.059894, -0.060007, -0.059998, -0.059875, -0.059644, -0.059311, -0.058882, -0.058361, -0.057753, -0.057063, -0.056295, -0.055453, -0.05454, -0.053561, -0.052518, -0.051414, -0.050253, -0.049036, -0.047766, -0.046446, -0.045077, -0.043661, -0.0422, -0.040696, -0.039149, -0.037562, -0.035934, -0.034267, -0.032562, -0.030819, -0.029039, -0.027222, -0.025368, -0.023478, -0.021551, -0.019588, -0.017589, -0.015557, -0.013497, -0.011419, -0.009346, -0.007317, -0.005396, -0.003653, -0.002138, -0.00126]</Zcoordinate>
//                </Airfoil_Geometry>
//                <Airfoil_Aerodynamics id="32991299" level="4">
//                    <Alpha_zero_lift from="input" unit="°" varName="_alphaZeroLift">-2.5000</Alpha_zero_lift>
//                    <Alpha_end_linear from="input" unit="°" varName="_alphaStar">10.0000</Alpha_end_linear>
//                    <Alpha_stall from="input" unit="°" varName="_alphaStall">12.0000</Alpha_stall>
//                    <Cl_alpha from="input" unit="" varName="_clAlpha">6.1000</Cl_alpha>
//                    <Cd_min from="input" unit="" varName="_cdMin">0.0250</Cd_min>
//                    <Cl_at_Cdmin from="input" unit="" varName="_clAtCdMin">0.2000</Cl_at_Cdmin>
//                    <Cl_end_linear from="input" unit="" varName="_clStar">0.8000</Cl_end_linear>
//                    <Cl_max from="input" unit="" varName="_clMax">1.3000</Cl_max>
//                    <K_factor_drag from="input" unit="" varName="_kFactorDragPolar">0.0750</K_factor_drag>
//                    <Cm_alpha from="input" unit="" varName="_cmAlphaAC">0.000000</Cm_alpha>
//                    <Xac from="input" unit="" varName="_aerodynamicCenterX">0.2500</Xac>
//                    <CmAC from="input" unit="" varName="_cmAC">-0.070000</CmAC>
//                    <CmAC_at_stall from="input" unit="" varName="_cmACStall">-0.090000</CmAC_at_stall>
//                </Airfoil_Aerodynamics>
//            </Airfoil_2>
//        </Actual_VTail_parameters>
//    </VTail>
//    <Nacelle id="7">
//        <Nacelle_parameters>
//            <Xcoordinate from="input" unit="m" varName="_X0">22.0000</Xcoordinate>
//            <Ycoordinate from="input" unit="m" varName="_Y0">2.7000</Ycoordinate>
//            <Zcoordinate from="input" unit="m" varName="_Z0">0.4000</Zcoordinate>
//            <Lenght from="input" unit="m" varName="_length">5.1000</Lenght>
//            <Mean_diameter from="input" unit="m" varName="_diameterMean">1.3000</Mean_diameter>
//            <Inlet_diameter from="input" unit="m" varName="_diameterInlet">1.2000</Inlet_diameter>
//            <Outlet_diameter from="input" unit="m" varName="_diameterOutlet">0.2000</Outlet_diameter>
//            <Reference_mass from="input" unit="kg" varName="_massReference">409.4000</Reference_mass>
//            <Wetted_surface unit="m²">41.6575</Wetted_surface>
//            <FormFactor unit="">1.3970</FormFactor>
//        </Nacelle_parameters>
//    </Nacelle>
//    <Fuel_tank id="5">
//        <Fuel_tank_parameters>
//            <Xcoordinate from="input" unit="m" varName="_X0">12.5000</Xcoordinate>
//            <Ycoordinate from="input" unit="m" varName="_Y0">0.0000</Ycoordinate>
//            <Zcoordinate from="input" unit="m" varName="_Z0">0.0000</Zcoordinate>
//            <Fuel_density from="input" unit="(kg/m³)*0.001" varName="_fuelDensity">0.8000</Fuel_density>
//            <Fuel_volume from="input" unit="L" varName="_fuelVolume">13365.0000</Fuel_volume>
//            <Fuel_mass from="input" unit="kg" varName="_fuelMass">10692.0000</Fuel_mass>
//            <LE_spanwise_extension unit="m">10.5094</LE_spanwise_extension>
//            <TE_spanwise_extension unit="m">10.5094</TE_spanwise_extension>
//            <Chordwise_mean_extension unit="m">2.9154</Chordwise_mean_extension>
//            <Root_height unit="m">0.4270</Root_height>
//            <Tip_height unit="m">0.4270</Tip_height>
//            <LE_surface unit="m²">4.4879</LE_surface>
//            <TE_surface unit="m²">4.4879</TE_surface>
//            <Volume unit="m³">0.0000</Volume>
//        </Fuel_tank_parameters>
//    </Fuel_tank>
//    <PowerPlant id="8">
//        <Power_plant_parameters>
//            <Type from="input" unit="" varName="_engineType">TURBOFAN</Type>
//            <Xcoordinate from="input" unit="m" varName="_X0">0.0000</Xcoordinate>
//            <Ycoordinate from="input" unit="m" varName="_Y0">0.0000</Ycoordinate>
//            <Zcoordinate from="input" unit="m" varName="_Z0">0.0000</Zcoordinate>
//            <Number_of_engines from="input" unit="" varName="_engineNumber">2</Number_of_engines>
//            <Mounting_point from="input" unit="" varName="_position">REAR_FUSELAGE</Mounting_point>
//            <Maximum_power_output from="input" unit="W" varName="_P0">1588677.8000</Maximum_power_output>
//            <Maximum_thrust from="input" unit="N" varName="_T0">62820.0000</Maximum_thrust>
//            <Maximum_total_power_output unit="W">3177355.6000</Maximum_total_power_output>
//            <Maximum_total_thrust unit="N">125640.0000</Maximum_total_thrust>
//            <Dry_engine_mass from="input" unit="kg" varName="_massDryEngineActual">1501.4000</Dry_engine_mass>
//            <Total_Reference_mass from="input" unit="kg" varName="_totalPowerPlantMassActual">4182.8000</Total_Reference_mass>
//        </Power_plant_parameters>
//    </PowerPlant>
//    <Systems id="9">
//        <Systems_data>
//            <Reference_mass from="input" unit="kg" varName="_massReference">2755.0000</Reference_mass>
//        </Systems_data>
//    </Systems>
//    <LandingGear id="6">
//        <Landing_gear_parameters>
//            <Xcoordinate from="input" unit="m" varName="_X0">5.0000</Xcoordinate>
//            <Ycoordinate from="input" unit="m" varName="_Y0">0.0000</Ycoordinate>
//            <Zcoordinate from="input" unit="m" varName="_Z0">0.0000</Zcoordinate>
//            <Mounting_point from="input" unit="">FUSELAGE</Mounting_point>
//            <Lenght from="input" unit="">not_initialized</Lenght>
//            <Reference_mass from="input" unit="kg" varName="_massReference">1459.0000</Reference_mass>
//        </Landing_gear_parameters>
//    </LandingGear>
//</AIRCRAFT>

}
