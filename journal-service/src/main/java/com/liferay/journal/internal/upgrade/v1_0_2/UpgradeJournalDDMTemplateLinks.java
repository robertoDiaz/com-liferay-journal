/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.journal.internal.upgrade.v1_0_2;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLinkLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Roberto Díaz
 */
public class UpgradeJournalDDMTemplateLinks extends UpgradeProcess {

	public UpgradeJournalDDMTemplateLinks(
		DDMTemplateLinkLocalService ddmTemplateLinkLocalService) {

		_ddmTemplateLinkLocalService = ddmTemplateLinkLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			long ddmStructureClassNameId = PortalUtil.getClassNameId(
				DDMStructure.class.getName());

			StringBundler sb = new StringBundler(4);

			sb.append("select DDMTemplateLink.templateLinkId ");
			sb.append("from DDMTemplateLink inner join JournalArticle on (");
			sb.append("DDMTemplateLink.classPK = JournalArticle.id_ and ");
			sb.append("DDMTemplateLink.classNameId = ?)");

			try (PreparedStatement ps1 = connection.prepareStatement(
				sb.toString())) {

				ps1.setLong(1, ddmStructureClassNameId);

				try (ResultSet rs = ps1.executeQuery()) {
					while (rs.next()) {
						long templateLinkId = rs.getLong("templateLinkId");

						try (PreparedStatement ps2 =
							connection.prepareStatement(
								"update DDMTemplateLink set classNameId = ? " +
								"where templateLinkId = ?")) {

							long journalArticleClassNameId =
								PortalUtil.getClassNameId(
									JournalArticle.class.getName());

							ps2.setLong(1, journalArticleClassNameId);
							ps2.setLong(2, templateLinkId);

							ps2.executeUpdate();
						}
					}
				}
			}
		}
	}

	private final DDMTemplateLinkLocalService _ddmTemplateLinkLocalService;

}