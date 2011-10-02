/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.common;

import java.util.Map;

import javolution.util.FastMap;

public final class JsLanguageFilesMapping {

    public static class datejs {

        private static Map<String, String> localeFiles = FastMap.newInstance();
        private static String defaultDateJs = "/images/jquery/plugins/datejs/date-en-US.js";

        static {
                        localeFiles.put("sq", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("sq_AL", "/images/jquery/plugins/datejs/date-sq-AL.js");
            localeFiles.put("ar", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("ar_DZ", "/images/jquery/plugins/datejs/date-ar-DZ.js");
            localeFiles.put("ar_BH", "/images/jquery/plugins/datejs/date-ar-BH.js");
            localeFiles.put("ar_EG", "/images/jquery/plugins/datejs/date-ar-EG.js");
            localeFiles.put("ar_IQ", "/images/jquery/plugins/datejs/date-ar-IQ.js");
            localeFiles.put("ar_JO", "/images/jquery/plugins/datejs/date-ar-JO.js");
            localeFiles.put("ar_KW", "/images/jquery/plugins/datejs/date-ar-KW.js");
            localeFiles.put("ar_LB", "/images/jquery/plugins/datejs/date-ar-LB.js");
            localeFiles.put("ar_LY", "/images/jquery/plugins/datejs/date-ar-LY.js");
            localeFiles.put("ar_MA", "/images/jquery/plugins/datejs/date-ar-MA.js");
            localeFiles.put("ar_OM", "/images/jquery/plugins/datejs/date-ar-OM.js");
            localeFiles.put("ar_QA", "/images/jquery/plugins/datejs/date-ar-QA.js");
            localeFiles.put("ar_SA", "/images/jquery/plugins/datejs/date-ar-SA.js");
            localeFiles.put("ar_SD", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("ar_SY", "/images/jquery/plugins/datejs/date-ar-SY.js");
            localeFiles.put("ar_TN", "/images/jquery/plugins/datejs/date-ar-TN.js");
            localeFiles.put("ar_AE", "/images/jquery/plugins/datejs/date-ar-AE.js");
            localeFiles.put("ar_YE", "/images/jquery/plugins/datejs/date-ar-YE.js");
            localeFiles.put("be", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("be_BY", "/images/jquery/plugins/datejs/date-be-BY.js");
            localeFiles.put("bg", "/images/jquery/plugins/datejs/date-bg-BG.js");
            localeFiles.put("bg_BG", "/images/jquery/plugins/datejs/date-bg-BG.js");
            localeFiles.put("ca", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("ca_ES", "/images/jquery/plugins/datejs/date-ca-ES.js");
            localeFiles.put("zh", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("zh_CN", "/images/jquery/plugins/datejs/date-zh-CN.js");
            localeFiles.put("zh_HK", "/images/jquery/plugins/datejs/date-zh-HK.js");
            localeFiles.put("zh_SG", "/images/jquery/plugins/datejs/date-zh-SG.js");
            localeFiles.put("zh_TW", "/images/jquery/plugins/datejs/date-zh-TW.js");
            localeFiles.put("hr", "/images/jquery/plugins/datejs/date-hr-HR.js");
            localeFiles.put("hr_HR", "/images/jquery/plugins/datejs/date-hr-HR.js");
            localeFiles.put("cs", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("cs_CZ", "/images/jquery/plugins/datejs/date-cs-CZ.js");
            localeFiles.put("da", "/images/jquery/plugins/datejs/date-da-DA.js");
            localeFiles.put("da_DK", "/images/jquery/plugins/datejs/date-da-DK.js");
            localeFiles.put("nl", "/images/jquery/plugins/datejs/date-nl-NL.js");
            localeFiles.put("nl_BE", "/images/jquery/plugins/datejs/date-nl-BE.js");
            localeFiles.put("nl_NL", "/images/jquery/plugins/datejs/date-nl-NL.js");
            localeFiles.put("en", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("en_AU", "/images/jquery/plugins/datejs/date-en-AU.js");
            localeFiles.put("en_CA", "/images/jquery/plugins/datejs/date-en-CA.js");
            localeFiles.put("en_IN", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("en_IE", "/images/jquery/plugins/datejs/date-en-IE.js");
            localeFiles.put("en_MT", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("en_NZ", "/images/jquery/plugins/datejs/date-en-NZ.js");
            localeFiles.put("en_PH", "/images/jquery/plugins/datejs/date-en-PH.js");
            localeFiles.put("en_SG", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("en_ZA", "/images/jquery/plugins/datejs/date-en-ZA.js");
            localeFiles.put("en_GB", "/images/jquery/plugins/datejs/date-en-GB.js");
            localeFiles.put("en_US", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("et", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("et_EE", "/images/jquery/plugins/datejs/date-et-EE.js");
            localeFiles.put("fi", "/images/jquery/plugins/datejs/date-fi-FI.js");
            localeFiles.put("fi_FI", "/images/jquery/plugins/datejs/date-fi-FI.js");
            localeFiles.put("fr", "/images/jquery/plugins/datejs/date-fr-FR.js");
            localeFiles.put("fr_BE", "/images/jquery/plugins/datejs/date-fr-BE.js");
            localeFiles.put("fr_CA", "/images/jquery/plugins/datejs/date-fr-CA.js");
            localeFiles.put("fr_FR", "/images/jquery/plugins/datejs/date-fr-FR.js");
            localeFiles.put("fr_LU", "/images/jquery/plugins/datejs/date-fr-LU.js");
            localeFiles.put("fr_CH", "/images/jquery/plugins/datejs/date-fr-CH.js");
            localeFiles.put("de", "/images/jquery/plugins/datejs/date-de-DE.js");
            localeFiles.put("de_AT", "/images/jquery/plugins/datejs/date-de-AT.js");
            localeFiles.put("de_DE", "/images/jquery/plugins/datejs/date-de-DE.js");
            localeFiles.put("de_LU", "/images/jquery/plugins/datejs/date-de-LU.js");
            localeFiles.put("de_CH", "/images/jquery/plugins/datejs/date-de-CH.js");
            localeFiles.put("el", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("el_CY", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("el_GR", "/images/jquery/plugins/datejs/date-el-GR.js");
            localeFiles.put("iw", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("iw_IL", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("hi_IN", "/images/jquery/plugins/datejs/date-hi-IN.js");
            localeFiles.put("hu", "/images/jquery/plugins/datejs/date-hu-HU.js");
            localeFiles.put("hu_HU", "/images/jquery/plugins/datejs/date-hu-HU.js");
            localeFiles.put("is", "/images/jquery/plugins/datejs/date-is-IS.js");
            localeFiles.put("is_IS", "/images/jquery/plugins/datejs/date-is-IS.js");
            localeFiles.put("in", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("in_ID", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("ga", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("ga_IE", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("it", "/images/jquery/plugins/datejs/date-it-IT.js");
            localeFiles.put("it_IT", "/images/jquery/plugins/datejs/date-it-IT.js");
            localeFiles.put("it_CH", "/images/jquery/plugins/datejs/date-it-CH.js");
            localeFiles.put("ja", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("ja_JP", "/images/jquery/plugins/datejs/date-ja-JP.js");
            localeFiles.put("ja_JP_JP", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("ko", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("ko_KR", "/images/jquery/plugins/datejs/date-ko-KR.js");
            localeFiles.put("lv", "/images/jquery/plugins/datejs/date-lv-LV.js");
            localeFiles.put("lv_LV", "/images/jquery/plugins/datejs/date-lv-LV.js");
            localeFiles.put("lt", "/images/jquery/plugins/datejs/date-lt-LT.js");
            localeFiles.put("lt_LT", "/images/jquery/plugins/datejs/date-lt-LT.js");
            localeFiles.put("mk", "/images/jquery/plugins/datejs/date-mk-MK.js");
            localeFiles.put("mk_MK", "/images/jquery/plugins/datejs/date-mk-MK.js");
            localeFiles.put("ms", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("ms_MY", "/images/jquery/plugins/datejs/date-ms-MY.js");
            localeFiles.put("mt", "/images/jquery/plugins/datejs/date-mt-MT.js");
            localeFiles.put("mt_MT", "/images/jquery/plugins/datejs/date-mt-MT.js");
            localeFiles.put("no", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("no_NO", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("no_NO_NY", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("pl", "/images/jquery/plugins/datejs/date-pl-PL.js");
            localeFiles.put("pl_PL", "/images/jquery/plugins/datejs/date-pl-PL.js");
            localeFiles.put("pt", "/images/jquery/plugins/datejs/date-pt-PT.js");
            localeFiles.put("pt_BR", "/images/jquery/plugins/datejs/date-pt-BR.js");
            localeFiles.put("pt_PT", "/images/jquery/plugins/datejs/date-pt-PT.js");
            localeFiles.put("ro", "/images/jquery/plugins/datejs/date-ro-RO.js");
            localeFiles.put("ro_RO", "/images/jquery/plugins/datejs/date-ro-RO.js");
            localeFiles.put("ru", "/images/jquery/plugins/datejs/date-ru-RU.js");
            localeFiles.put("ru_RU", "/images/jquery/plugins/datejs/date-ru-RU.js");
            localeFiles.put("sr", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("sr_BA", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("sr_ME", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("sr_CS", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("sr_RS", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("sk", "/images/jquery/plugins/datejs/date-sk-SK.js");
            localeFiles.put("sk_SK", "/images/jquery/plugins/datejs/date-sk-SK.js");
            localeFiles.put("sl", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("sl_SI", "/images/jquery/plugins/datejs/date-sl-SI.js");
            localeFiles.put("es", "/images/jquery/plugins/datejs/date-es-ES.js");
            localeFiles.put("es_AR", "/images/jquery/plugins/datejs/date-es-AR.js");
            localeFiles.put("es_BO", "/images/jquery/plugins/datejs/date-es-BO.js");
            localeFiles.put("es_CL", "/images/jquery/plugins/datejs/date-es-CL.js");
            localeFiles.put("es_CO", "/images/jquery/plugins/datejs/date-es-CO.js");
            localeFiles.put("es_CR", "/images/jquery/plugins/datejs/date-es-CR.js");
            localeFiles.put("es_DO", "/images/jquery/plugins/datejs/date-es-DO.js");
            localeFiles.put("es_EC", "/images/jquery/plugins/datejs/date-es-EC.js");
            localeFiles.put("es_SV", "/images/jquery/plugins/datejs/date-es-SV.js");
            localeFiles.put("es_GT", "/images/jquery/plugins/datejs/date-es-GT.js");
            localeFiles.put("es_HN", "/images/jquery/plugins/datejs/date-es-HN.js");
            localeFiles.put("es_MX", "/images/jquery/plugins/datejs/date-es-MX.js");
            localeFiles.put("es_NI", "/images/jquery/plugins/datejs/date-es-NI.js");
            localeFiles.put("es_PA", "/images/jquery/plugins/datejs/date-es-PA.js");
            localeFiles.put("es_PY", "/images/jquery/plugins/datejs/date-es-PY.js");
            localeFiles.put("es_PE", "/images/jquery/plugins/datejs/date-es-PE.js");
            localeFiles.put("es_PR", "/images/jquery/plugins/datejs/date-es-PR.js");
            localeFiles.put("es_ES", "/images/jquery/plugins/datejs/date-es-ES.js");
            localeFiles.put("es_US", "/images/jquery/plugins/datejs/date-es-ES.js");
            localeFiles.put("es_UY", "/images/jquery/plugins/datejs/date-es-UY.js");
            localeFiles.put("es_VE", "/images/jquery/plugins/datejs/date-es-VE.js");
            localeFiles.put("sv", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("sv_SE", "/images/jquery/plugins/datejs/date-sv-SE.js");
            localeFiles.put("th", "/images/jquery/plugins/datejs/date-th-TH.js");
            localeFiles.put("th_TH", "/images/jquery/plugins/datejs/date-th-TH.js");
            localeFiles.put("th_TH_TH", "/images/jquery/plugins/datejs/date-th-TH.js");
            localeFiles.put("tr", "/images/jquery/plugins/datejs/date-tr-TR.js");
            localeFiles.put("tr_TR", "/images/jquery/plugins/datejs/date-tr-TR.js");
            localeFiles.put("uk", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("uk_UA", "/images/jquery/plugins/datejs/date-uk-UA.js");
            localeFiles.put("vi", "/images/jquery/plugins/datejs/date-en-US.js");
            localeFiles.put("vi_VN", "/images/jquery/plugins/datejs/date-vi-VN.js");
        }

        public static String getFilePath(String locale) {
            if (datejs.localeFiles.get(locale) == null) {
                return datejs.defaultDateJs;
            }
            return datejs.localeFiles.get(locale);
        }

    }

    public static class jquery {
        private static Map<String, String> localeFiles = FastMap.newInstance();
        private static String defaultDateJs = "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js";

        static {
            localeFiles.put("sq", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sq.js");
            localeFiles.put("sq_AL", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sq.js");
            localeFiles.put("ar", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_DZ", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_BH", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_EG", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_IQ", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_JO", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_KW", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_LB", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_LY", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_MA", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_OM", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_QA", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_SA", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_SD", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_SY", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_TN", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_AE", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("ar_YE", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ar.js");
            localeFiles.put("be", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("be_BY", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("bg", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-bg.js");
            localeFiles.put("bg_BG", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-bg.js");
            localeFiles.put("ca", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ca.js");
            localeFiles.put("ca_ES", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ca.js");
            localeFiles.put("zh", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("zh_CN", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-zh-CN.js");
            localeFiles.put("zh_HK", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-zh-HK.js");
            localeFiles.put("zh_SG", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("zh_TW", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-zh-TW.js");
            localeFiles.put("hr", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-hr.js");
            localeFiles.put("hr_HR", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-hr.js");
            localeFiles.put("cs", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-cs.js");
            localeFiles.put("cs_CZ", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-cs.js");
            localeFiles.put("da", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-da.js");
            localeFiles.put("da_DK", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-da.js");
            localeFiles.put("nl", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-nl.js");
            localeFiles.put("nl_BE", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-nl.js");
            localeFiles.put("nl_NL", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-nl.js");
            localeFiles.put("en", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("en_AU", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("en_CA", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("en_IN", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("en_IE", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("en_MT", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("en_NZ", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("en_PH", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("en_SG", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("en_ZA", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("en_GB", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("en_US", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("et", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-et.js");
            localeFiles.put("et_EE", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-et.js");
            localeFiles.put("fi", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-fi.js");
            localeFiles.put("fi_FI", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-fi.js");
            localeFiles.put("fr", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-fr.js");
            localeFiles.put("fr_BE", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-fr.js");
            localeFiles.put("fr_CA", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-fr.js");
            localeFiles.put("fr_FR", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-fr.js");
            localeFiles.put("fr_LU", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-fr.js");
            localeFiles.put("fr_CH", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-fr.js");
            localeFiles.put("de", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-de.js");
            localeFiles.put("de_AT", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-de.js");
            localeFiles.put("de_DE", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-de.js");
            localeFiles.put("de_LU", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-de.js");
            localeFiles.put("de_CH", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-de.js");
            localeFiles.put("el", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-el.js");
            localeFiles.put("el_CY", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-el.js");
            localeFiles.put("el_GR", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-el.js");
            localeFiles.put("iw", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("iw_IL", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("hi_IN", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("hu", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-hu.js");
            localeFiles.put("hu_HU", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-hu.js");
            localeFiles.put("is", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-is.js");
            localeFiles.put("is_IS", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-is.js");
            localeFiles.put("in", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("in_ID", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("ga", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("ga_IE", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("it", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-it.js");
            localeFiles.put("it_IT", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-it.js");
            localeFiles.put("it_CH", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-it.js");
            localeFiles.put("ja", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ja.js");
            localeFiles.put("ja_JP", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ja.js");
            localeFiles.put("ja_JP_JP", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ja.js");
            localeFiles.put("ko", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ko.js");
            localeFiles.put("ko_KR", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ko.js");
            localeFiles.put("lv", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-lv.js");
            localeFiles.put("lv_LV", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-lv.js");
            localeFiles.put("lt", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-lt.js");
            localeFiles.put("lt_LT", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-lt.js");
            localeFiles.put("mk", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("mk_MK", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("ms", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ms.js");
            localeFiles.put("ms_MY", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ms.js");
            localeFiles.put("mt", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("mt_MT", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-en.js");
            localeFiles.put("no", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-no.js");
            localeFiles.put("no_NO", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-no.js");
            localeFiles.put("no_NO_NY", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-no.js");
            localeFiles.put("pl", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-pl.js");
            localeFiles.put("pl_PL", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-pl.js");
            localeFiles.put("pt", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-pt.js");
            localeFiles.put("pt_BR", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-pt.js");
            localeFiles.put("pt_PT", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-pt.js");
            localeFiles.put("ro", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ro.js");
            localeFiles.put("ro_RO", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ro.js");
            localeFiles.put("ru", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ru.js");
            localeFiles.put("ru_RU", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-ru.js");
            localeFiles.put("sr", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sr.js");
            localeFiles.put("sr_BA", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sr.js");
            localeFiles.put("sr_ME", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sr.js");
            localeFiles.put("sr_CS", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sr.js");
            localeFiles.put("sr_RS", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sr.js");
            localeFiles.put("sk", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sk.js");
            localeFiles.put("sk_SK", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sk.js");
            localeFiles.put("sl", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sl.js");
            localeFiles.put("sl_SI", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sl.js");
            localeFiles.put("es", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_AR", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_BO", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_CL", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_CO", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_CR", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_DO", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_EC", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_SV", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_GT", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_HN", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_MX", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_NI", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_PA", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_PY", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_PE", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_PR", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_ES", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_US", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_UY", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("es_VE", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-es.js");
            localeFiles.put("sv", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sv.js");
            localeFiles.put("sv_SE", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-sv.js");
            localeFiles.put("th", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-th.js");
            localeFiles.put("th_TH", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-th.js");
            localeFiles.put("th_TH_TH", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-th.js");
            localeFiles.put("tr", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-tr.js");
            localeFiles.put("tr_TR", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-tr.js");
            localeFiles.put("uk", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-uk.js");
            localeFiles.put("uk_UA", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-uk.js");
            localeFiles.put("vi", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-vi.js");
            localeFiles.put("vi_VN", "/images/jquery/ui/development-bundle/ui/i18n/jquery.ui.datepicker-vi.js");
        }

        public static String getFilePath(String locale) {
            if (datejs.localeFiles.get(locale) == null) {
                return jquery.defaultDateJs;
            }
            return jquery.localeFiles.get(locale);
        }

    }

    public static class validation {
        private static Map<String, String> localeFiles = FastMap.newInstance();
        private static String defaultValidation = "/images/webapp/images/jquery/plugins/validate/localization/messages_en.js";

        static {
            localeFiles.put("sq", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("sq_AL", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("ar", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_DZ", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_BH", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_EG", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_IQ", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_JO", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_KW", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_LB", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_LY", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_MA", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_OM", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_QA", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_SA", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_SD", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_SY", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_TN", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_AE", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("ar_YE", "/images/jquery/plugins/validate/localization/messages_ar.js");
            localeFiles.put("be", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("be_BY", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("bg", "/images/jquery/plugins/validate/localization/messages_bg.js");
            localeFiles.put("bg_BG", "/images/jquery/plugins/validate/localization/messages_bg.js");
            localeFiles.put("ca", "/images/jquery/plugins/validate/localization/messages_ca.js");
            localeFiles.put("ca_ES", "/images/jquery/plugins/validate/localization/messages_ca.js");
            localeFiles.put("zh", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("zh_CN", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("zh_HK", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("zh_SG", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("zh_TW", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("hr", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("hr_HR", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("cs", "/images/jquery/plugins/validate/localization/messages_cs.js");
            localeFiles.put("cs_CZ", "/images/jquery/plugins/validate/localization/messages_cs.js");
            localeFiles.put("da", "/images/jquery/plugins/validate/localization/messages_da.js");
            localeFiles.put("da_DK", "/images/jquery/plugins/validate/localization/messages_da.js");
            localeFiles.put("nl", "/images/jquery/plugins/validate/localization/messages_nl.js");
            localeFiles.put("nl_BE", "/images/jquery/plugins/validate/localization/messages_nl.js");
            localeFiles.put("nl_NL", "/images/jquery/plugins/validate/localization/messages_nl.js");
            localeFiles.put("en", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("en_AU", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("en_CA", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("en_IN", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("en_IE", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("en_MT", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("en_NZ", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("en_PH", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("en_SG", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("en_ZA", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("en_GB", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("en_US", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("et", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("et_EE", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("fi", "/images/jquery/plugins/validate/localization/messages_fi.js");
            localeFiles.put("fi_FI", "/images/jquery/plugins/validate/localization/messages_fi.js");
            localeFiles.put("fr", "/images/jquery/plugins/validate/localization/messages_fr.js");
            localeFiles.put("fr_BE", "/images/jquery/plugins/validate/localization/messages_fr.js");
            localeFiles.put("fr_CA", "/images/jquery/plugins/validate/localization/messages_fr.js");
            localeFiles.put("fr_FR", "/images/jquery/plugins/validate/localization/messages_fr.js");
            localeFiles.put("fr_LU", "/images/jquery/plugins/validate/localization/messages_fr.js");
            localeFiles.put("fr_CH", "/images/jquery/plugins/validate/localization/messages_fr.js");
            localeFiles.put("de", "/images/jquery/plugins/validate/localization/messages_de.js");
            localeFiles.put("de_AT", "/images/jquery/plugins/validate/localization/messages_de.js");
            localeFiles.put("de_DE", "/images/jquery/plugins/validate/localization/messages_de.js");
            localeFiles.put("de_LU", "/images/jquery/plugins/validate/localization/messages_de.js");
            localeFiles.put("de_CH", "/images/jquery/plugins/validate/localization/messages_de.js");
            localeFiles.put("el", "/images/jquery/plugins/validate/localization/messages_el.js");
            localeFiles.put("el_CY", "/images/jquery/plugins/validate/localization/messages_el.js");
            localeFiles.put("el_GR", "/images/jquery/plugins/validate/localization/messages_el.js");
            localeFiles.put("iw", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("iw_IL", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("hi_IN", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("hu", "/images/jquery/plugins/validate/localization/messages_hu.js");
            localeFiles.put("hu_HU", "/images/jquery/plugins/validate/localization/messages_hu.js");
            localeFiles.put("is", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("is_IS", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("in", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("in_ID", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("ga", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("ga_IE", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("it", "/images/jquery/plugins/validate/localization/messages_it.js");
            localeFiles.put("it_IT", "/images/jquery/plugins/validate/localization/messages_it.js");
            localeFiles.put("it_CH", "/images/jquery/plugins/validate/localization/messages_it.js");
            localeFiles.put("ja", "/images/jquery/plugins/validate/localization/messages_ja.js");
            localeFiles.put("ja_JP", "/images/jquery/plugins/validate/localization/messages_ja.js");
            localeFiles.put("ja_JP_JP", "/images/jquery/plugins/validate/localization/messages_ja.js");
            localeFiles.put("ko", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("ko_KR", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("lv", "/images/jquery/plugins/validate/localization/messages_lv.js");
            localeFiles.put("lv_LV", "/images/jquery/plugins/validate/localization/messages_lv.js");
            localeFiles.put("lt", "/images/jquery/plugins/validate/localization/messages_lt.js");
            localeFiles.put("lt_LT", "/images/jquery/plugins/validate/localization/messages_lt.js");
            localeFiles.put("mk", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("mk_MK", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("ms", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("ms_MY", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("mt", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("mt_MT", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("no", "/images/jquery/plugins/validate/localization/messages_no.js");
            localeFiles.put("no_NO", "/images/jquery/plugins/validate/localization/messages_no.js");
            localeFiles.put("no_NO_NY", "/images/jquery/plugins/validate/localization/messages_no.js");
            localeFiles.put("pl", "/images/jquery/plugins/validate/localization/messages_pl.js");
            localeFiles.put("pl_PL", "/images/jquery/plugins/validate/localization/messages_pl.js");
            localeFiles.put("pt", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("pt_BR", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("pt_PT", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("ro", "/images/jquery/plugins/validate/localization/messages_ro.js");
            localeFiles.put("ro_RO", "/images/jquery/plugins/validate/localization/messages_ro.js");
            localeFiles.put("ru", "/images/jquery/plugins/validate/localization/messages_ru.js");
            localeFiles.put("ru_RU", "/images/jquery/plugins/validate/localization/messages_ru.js");
            localeFiles.put("sr", "/images/jquery/plugins/validate/localization/messages_sr.js");
            localeFiles.put("sr_BA", "/images/jquery/plugins/validate/localization/messages_sr.js");
            localeFiles.put("sr_ME", "/images/jquery/plugins/validate/localization/messages_sr.js");
            localeFiles.put("sr_CS", "/images/jquery/plugins/validate/localization/messages_sr.js");
            localeFiles.put("sr_RS", "/images/jquery/plugins/validate/localization/messages_sr.js");
            localeFiles.put("sk", "/images/jquery/plugins/validate/localization/messages_sk.js");
            localeFiles.put("sk_SK", "/images/jquery/plugins/validate/localization/messages_sk.js");
            localeFiles.put("sl", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("sl_SI", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("es", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_AR", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_BO", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_CL", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_CO", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_CR", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_DO", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_EC", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_SV", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_GT", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_HN", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_MX", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_NI", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_PA", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_PY", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_PE", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_PR", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_ES", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_US", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_UY", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("es_VE", "/images/jquery/plugins/validate/localization/messages_es.js");
            localeFiles.put("sv", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("sv_SE", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("th", "/images/jquery/plugins/validate/localization/messages_th.js");
            localeFiles.put("th_TH", "/images/jquery/plugins/validate/localization/messages_th.js");
            localeFiles.put("th_TH_TH", "/images/jquery/plugins/validate/localization/messages_th.js");
            localeFiles.put("tr", "/images/jquery/plugins/validate/localization/messages_tr.js");
            localeFiles.put("tr_TR", "/images/jquery/plugins/validate/localization/messages_tr.js");
            localeFiles.put("uk", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("uk_UA", "/images/jquery/plugins/validate/localization/messages_en.js");
            localeFiles.put("vi", "/images/jquery/plugins/validate/localization/messages_vi.js");
            localeFiles.put("vi_VN", "/images/jquery/plugins/validate/localization/messages_vi.js");
        }

        public static String getFilePath(String locale) {
            if (validation.localeFiles.get(locale) == null) {
                return validation.defaultValidation;
            }
            return validation.localeFiles.get(locale);
        }
    }

    public static class dateTime {
        private static Map<String, String> localeFiles = FastMap.newInstance();

        static {
            localeFiles.put("sq", "");
            localeFiles.put("sq_AL", "");
            localeFiles.put("ar", "");
            localeFiles.put("ar_DZ", "");
            localeFiles.put("ar_BH", "");
            localeFiles.put("ar_EG", "");
            localeFiles.put("ar_IQ", "");
            localeFiles.put("ar_JO", "");
            localeFiles.put("ar_KW", "");
            localeFiles.put("ar_LB", "");
            localeFiles.put("ar_LY", "");
            localeFiles.put("ar_MA", "");
            localeFiles.put("ar_OM", "");
            localeFiles.put("ar_QA", "");
            localeFiles.put("ar_SA", "");
            localeFiles.put("ar_SD", "");
            localeFiles.put("ar_SY", "");
            localeFiles.put("ar_TN", "");
            localeFiles.put("ar_AE", "");
            localeFiles.put("ar_YE", "");
            localeFiles.put("be", "");
            localeFiles.put("be_BY", "");
            localeFiles.put("bg", "");
            localeFiles.put("bg_BG", "");
            localeFiles.put("ca", "");
            localeFiles.put("ca_ES", "");
            localeFiles.put("zh", "");
            localeFiles.put("zh_CN", "");
            localeFiles.put("zh_HK", "");
            localeFiles.put("zh_SG", "");
            localeFiles.put("zh_TW", "");
            localeFiles.put("hr", "");
            localeFiles.put("hr_HR", "");
            localeFiles.put("cs", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-cs.js");
            localeFiles.put("cs_CZ", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-cs.js");
            localeFiles.put("da", "");
            localeFiles.put("da_DK", "");
            localeFiles.put("nl", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-nl.js");
            localeFiles.put("nl_BE", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-nl.js");
            localeFiles.put("nl_NL", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-nl.js");
            localeFiles.put("en", "");
            localeFiles.put("en_AU", "");
            localeFiles.put("en_CA", "");
            localeFiles.put("en_IN", "");
            localeFiles.put("en_IE", "");
            localeFiles.put("en_MT", "");
            localeFiles.put("en_NZ", "");
            localeFiles.put("en_PH", "");
            localeFiles.put("en_SG", "");
            localeFiles.put("en_ZA", "");
            localeFiles.put("en_GB", "");
            localeFiles.put("en_US", "");
            localeFiles.put("et", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-et.js");
            localeFiles.put("et_EE", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-et.js");
            localeFiles.put("fi", "");
            localeFiles.put("fi_FI", "");
            localeFiles.put("fr", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-fr.js");
            localeFiles.put("fr_BE", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-fr.js");
            localeFiles.put("fr_CA", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-fr.js");
            localeFiles.put("fr_FR", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-fr.js");
            localeFiles.put("fr_LU", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-fr.js");
            localeFiles.put("fr_CH", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-fr.js");
            localeFiles.put("de", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-de.js");
            localeFiles.put("de_AT", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-de.js");
            localeFiles.put("de_DE", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-de.js");
            localeFiles.put("de_LU", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-de.js");
            localeFiles.put("de_CH", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-de.js");
            localeFiles.put("el", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-el.js");
            localeFiles.put("el_CY", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-el.js");
            localeFiles.put("el_GR", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-el.js");
            localeFiles.put("iw", "");
            localeFiles.put("iw_IL", "");
            localeFiles.put("hi_IN", "");
            localeFiles.put("hu", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-hu.js");
            localeFiles.put("hu_HU", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-hu.js");
            localeFiles.put("is", "");
            localeFiles.put("is_IS", "");
            localeFiles.put("in", "");
            localeFiles.put("in_ID", "");
            localeFiles.put("ga", "");
            localeFiles.put("ga_IE", "");
            localeFiles.put("it", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-it.js");
            localeFiles.put("it_IT", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-it.js");
            localeFiles.put("it_CH", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-it.js");
            localeFiles.put("ja", "");
            localeFiles.put("ja_JP", "");
            localeFiles.put("ja_JP_JP", "");
            localeFiles.put("ko", "");
            localeFiles.put("ko_KR", "");
            localeFiles.put("lv", "");
            localeFiles.put("lv_LV", "");
            localeFiles.put("lt", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-lt.js");
            localeFiles.put("lt_LT", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-lt.js");
            localeFiles.put("mk", "");
            localeFiles.put("mk_MK", "");
            localeFiles.put("ms", "");
            localeFiles.put("ms_MY", "");
            localeFiles.put("mt", "");
            localeFiles.put("mt_MT", "");
            localeFiles.put("no", "");
            localeFiles.put("no_NO", "");
            localeFiles.put("no_NO_NY", "");
            localeFiles.put("pl", "");
            localeFiles.put("pl_PL", "");
            localeFiles.put("pt", "");
            localeFiles.put("pt_BR", "");
            localeFiles.put("pt_PT", "");
            localeFiles.put("ro", "");
            localeFiles.put("ro_RO", "");
            localeFiles.put("ru", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-ru.js");
            localeFiles.put("ru_RU", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-ru.js");
            localeFiles.put("sr", "");
            localeFiles.put("sr_BA", "");
            localeFiles.put("sr_ME", "");
            localeFiles.put("sr_CS", "");
            localeFiles.put("sr_RS", "");
            localeFiles.put("sk", "");
            localeFiles.put("sk_SK", "");
            localeFiles.put("sl", "");
            localeFiles.put("sl_SI", "");
            localeFiles.put("es", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_AR", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_BO", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_CL", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_CO", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_CR", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_DO", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_EC", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_SV", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_GT", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_HN", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_MX", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_NI", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_PA", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_PY", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_PE", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_PR", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_ES", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_US", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_UY", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("es_VE", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-es.js");
            localeFiles.put("sv", "");
            localeFiles.put("sv_SE", "");
            localeFiles.put("th", "");
            localeFiles.put("th_TH", "");
            localeFiles.put("th_TH_TH", "");
            localeFiles.put("tr", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-tr.js");
            localeFiles.put("tr_TR", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-tr.js");
            localeFiles.put("uk", "");
            localeFiles.put("uk_UA", "");
            localeFiles.put("vi", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-vi.js");
            localeFiles.put("vi_VN", "/images/jquery/plugins/datetimepicker/localization/jquery-ui-timepicker-vi.js");
        }

        public static String getFilePath(String locale) {
            if (dateTime.localeFiles.get(locale) == null) {
                return null;
            }
            return dateTime.localeFiles.get(locale);
        }
    }
}
