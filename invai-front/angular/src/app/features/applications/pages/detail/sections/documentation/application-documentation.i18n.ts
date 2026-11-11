export const APPLICATION_DOCUMENTATION_TEXTS = {
  title: $localize`:@@applicationDocumentationTitle:Documentació`,
  errorSummary: $localize`:@@applicationDocumentationErrorSummary:Error`,
  infoSummary: $localize`:@@applicationDocumentationInfoSummary:Informació`,
  downloadError: $localize`:@@applicationDocumentationDownloadError:No s'ha pogut descarregar el fitxer.`,
  view: $localize`:@@applicationDocumentationActionView:Consulta`,
  download: $localize`:@@applicationDocumentationActionDownload:Descarrega`,
  delete: $localize`:@@applicationDocumentationActionDelete:Elimina`,
  actionsHeader: $localize`:@@applicationDocumentationActionsHeader:Accions`,
  required: $localize`:@@applicationDocumentationRequired:Camp obligatori`,
  maxLength: (max: number) =>
    $localize`:@@applicationDocumentationMaxLength:Ha de tenir com a màxim ${max}:max: caràcters`,
  deleteCancel: $localize`:@@applicationDocumentationDeleteCancel:Cancel·lar`,
  deleteConfirm: $localize`:@@applicationDocumentationDeleteConfirm:Eliminar`,
  fileTooLarge: $localize`:@@applicationDocumentationFileTooLarge:El fitxer supera la mida màxima de 5 MB.`,
  fileTypeNotAllowed: $localize`:@@applicationDocumentationFileTypeNotAllowed:El format del fitxer no està admès.`,
  fileTooLargeRejected: (fileName: string) =>
    $localize`:@@applicationDocumentationFileTooLargeRejected:No s'ha adjuntat «${fileName}:fileName:»: supera la mida màxima de 5 MB.`,
  fileTypeRejected: (fileName: string) =>
    $localize`:@@applicationDocumentationFileTypeRejected:No s'ha adjuntat «${fileName}:fileName:»: el format del fitxer no està admès.`,
  formatsLoading: $localize`:@@applicationDocumentationFormatsLoading:S'estan carregant els formats admesos.`,
  formatsUnavailable: $localize`:@@applicationDocumentationFormatsUnavailable:No s'han pogut carregar els formats admesos: ara no es pot adjuntar cap fitxer.`,
};

export const APPLICATION_ATTACHMENTS_TEXTS = {
  title: $localize`:@@applicationAttachmentsTitle:Adjunts`,
  quickSearchPlaceholder: $localize`:@@applicationAttachmentsQuickSearchPlaceholder:Cerca per títol`,
  quickSearchAriaLabel: $localize`:@@applicationAttachmentsQuickSearchAriaLabel:Cerca ràpida d'adjunts`,
  addAriaLabel: $localize`:@@applicationAttachmentsAddAriaLabel:Afegeix un adjunt`,
  columnsAriaLabel: $localize`:@@applicationAttachmentsColumnsAriaLabel:Columnes dels adjunts`,
  filtersAriaLabel: $localize`:@@applicationAttachmentsFiltersAriaLabel:Filtres dels adjunts`,
  stateFilter: $localize`:@@applicationAttachmentsFilterState:Estat`,
  documentTypeFilter: $localize`:@@applicationAttachmentsFilterDocumentType:Tipus de document`,
  categoryFilter: $localize`:@@applicationAttachmentsFilterCategory:Categoria`,
  subcategoryFilter: $localize`:@@applicationAttachmentsFilterSubcategory:Subcategoria`,
  caption: $localize`:@@applicationAttachmentsCaption:Adjunts de l'aplicació`,
  empty: $localize`:@@applicationAttachmentsEmpty:No hi ha cap adjunt.`,
  rowActions: (title: string) =>
    $localize`:@@applicationAttachmentsRowActions:Accions de l'adjunt «${title}:title:»`,
  loadError: $localize`:@@applicationAttachmentsLoadError:No s'han pogut carregar els adjunts.`,
  createdTitle: $localize`:@@applicationAttachmentCreatedTitle:Adjunt afegit`,
  createdDetail: $localize`:@@applicationAttachmentCreatedDetail:L'adjunt s'ha afegit correctament.`,
  updatedTitle: $localize`:@@applicationAttachmentUpdatedTitle:Adjunt actualitzat`,
  updatedDetail: $localize`:@@applicationAttachmentUpdatedDetail:L'adjunt s'ha actualitzat correctament.`,
  deletedTitle: $localize`:@@applicationAttachmentDeletedTitle:Adjunt eliminat`,
  deletedDetail: $localize`:@@applicationAttachmentDeletedDetail:L'adjunt s'ha eliminat correctament.`,
  saveError: $localize`:@@applicationAttachmentSaveError:No s'ha pogut desar l'adjunt.`,
  deleteError: $localize`:@@applicationAttachmentDeleteError:No s'ha pogut eliminar l'adjunt.`,
  categoriesLoadError: $localize`:@@applicationAttachmentCategoriesLoadError:No s'han pogut carregar les categories documentals.`,
  catalogsLoadError: $localize`:@@applicationAttachmentCatalogsLoadError:No s'han pogut carregar els estats i els formats dels documents.`,
  deleteDialogTitle: $localize`:@@applicationAttachmentDeleteDialogTitle:Eliminar l'adjunt?`,
  deleteDialogMessage: (title: string) =>
    $localize`:@@applicationAttachmentDeleteDialogMessage:Estàs a punt d'eliminar l'adjunt «${title}:title:».`,
  deleteDialogCancelAriaLabel: $localize`:@@applicationAttachmentDeleteDialogCancel:Cancel·la l'eliminació de l'adjunt`,
  deleteDialogConfirmAriaLabel: $localize`:@@applicationAttachmentDeleteDialogConfirm:Confirma l'eliminació de l'adjunt`,
};

export const APPLICATION_ATTACHMENT_DIALOG_TEXTS = {
  titles: {
    create: $localize`:@@applicationAttachmentDialogCreate:Afegeix un adjunt`,
    view: $localize`:@@applicationAttachmentDialogView:Consulta de l'adjunt`,
    edit: $localize`:@@applicationAttachmentDialogEdit:Edició de l'adjunt`,
  },
  ariaLabels: {
    accept: $localize`:@@applicationAttachmentDialogAccept:Accepta la consulta de l'adjunt`,
    add: $localize`:@@applicationAttachmentDialogAdd:Afegeix l'adjunt`,
    cancel: $localize`:@@applicationAttachmentDialogCancel:Cancel·la els canvis de l'adjunt`,
    close: $localize`:@@applicationAttachmentDialogClose:Tanca el formulari de l'adjunt`,
    deactivate: $localize`:@@applicationAttachmentDialogDeactivate:Elimina l'adjunt`,
    edit: $localize`:@@applicationAttachmentDialogEditAction:Edita l'adjunt`,
    restore: $localize`:@@applicationAttachmentDialogRestore:Restaura l'adjunt`,
    save: $localize`:@@applicationAttachmentDialogSave:Desa els canvis de l'adjunt`,
  },
  labels: {
    title: $localize`:@@applicationAttachmentDialogTitleLabel:Títol`,
    file: $localize`:@@applicationAttachmentDialogFile:Fitxer`,
    documentType: $localize`:@@applicationAttachmentDialogDocumentType:Tipus de document`,
    category: $localize`:@@applicationAttachmentDialogCategory:Categoria`,
    subcategory: $localize`:@@applicationAttachmentDialogSubcategory:Subcategoria (opcional)`,
    subcategoryView: $localize`:@@applicationAttachmentDialogSubcategoryView:Subcategoria`,
    version: $localize`:@@applicationAttachmentDialogVersion:Versió`,
    state: $localize`:@@applicationAttachmentDialogState:Estat`,
  },
  requiredHint: $localize`:@@applicationAttachmentDialogRequiredHint:Tots els camps són obligatoris, excepte els indicats com a opcionals.`,
  dropHint: $localize`:@@applicationAttachmentDialogDropHint:Arrossega el fitxer aquí per carregar-lo.`,
  fileLimitsHint: $localize`:@@applicationAttachmentDialogFileLimitsHint:Mida màxima: 5 MB.`,
  actions: {
    attach: $localize`:@@applicationAttachmentDialogAttach:Adjunta`,
    cancel: $localize`:@@applicationAttachmentDialogCancelLabel:Cancel·la`,
    save: $localize`:@@applicationAttachmentDialogSaveLabel:Desa`,
    removeFile: $localize`:@@applicationAttachmentDialogRemoveFile:Elimina el fitxer seleccionat`,
  },
  noSubcategory: $localize`:@@applicationAttachmentDialogNoSubcategory:Sense subcategoria`,
};

export const APPLICATION_LINKS_TEXTS = {
  title: $localize`:@@applicationLinksTitle:Enllaços`,
  quickSearchPlaceholder: $localize`:@@applicationLinksQuickSearchPlaceholder:Cerca per títol, descripció, URL o categoria`,
  quickSearchAriaLabel: $localize`:@@applicationLinksQuickSearchAriaLabel:Cerca ràpida d'enllaços`,
  addAriaLabel: $localize`:@@applicationLinksAddAriaLabel:Afegeix un enllaç`,
  columnsAriaLabel: $localize`:@@applicationLinksColumnsAriaLabel:Columnes dels enllaços`,
  filtersAriaLabel: $localize`:@@applicationLinksFiltersAriaLabel:Filtres dels enllaços`,
  categoryFilter: $localize`:@@applicationLinksFilterCategory:Categoria`,
  caption: $localize`:@@applicationLinksCaption:Enllaços de l'aplicació`,
  empty: $localize`:@@applicationLinksEmpty:No hi ha cap enllaç.`,
  rowActions: (title: string) =>
    $localize`:@@applicationLinksRowActions:Accions de l'enllaç «${title}:title:»`,
  opensInNewTab: $localize`:@@applicationLinksOpensInNewTab:(s'obre en una pestanya nova)`,
  loadError: $localize`:@@applicationLinksLoadError:No s'han pogut carregar els enllaços.`,
  categoriesLoadError: $localize`:@@applicationLinksCategoriesLoadError:No s'han pogut carregar les categories documentals.`,
  createdTitle: $localize`:@@applicationLinkCreatedTitle:Enllaç afegit`,
  createdDetail: $localize`:@@applicationLinkCreatedDetail:L'enllaç s'ha afegit correctament.`,
  updatedTitle: $localize`:@@applicationLinkUpdatedTitle:Enllaç actualitzat`,
  updatedDetail: $localize`:@@applicationLinkUpdatedDetail:L'enllaç s'ha actualitzat correctament.`,
  deletedTitle: $localize`:@@applicationLinkDeletedTitle:Enllaç eliminat`,
  deletedDetail: $localize`:@@applicationLinkDeletedDetail:L'enllaç s'ha eliminat correctament.`,
  saveError: $localize`:@@applicationLinkSaveError:No s'ha pogut desar l'enllaç.`,
  deleteError: $localize`:@@applicationLinkDeleteError:No s'ha pogut eliminar l'enllaç.`,
  deleteDialogTitle: $localize`:@@applicationLinkDeleteDialogTitle:Eliminar l'enllaç?`,
  deleteDialogMessage: (title: string) =>
    $localize`:@@applicationLinkDeleteDialogMessage:Estàs a punt d'eliminar l'enllaç «${title}:title:».`,
  deleteDialogCancelAriaLabel: $localize`:@@applicationLinkDeleteDialogCancel:Cancel·la l'eliminació de l'enllaç`,
  deleteDialogConfirmAriaLabel: $localize`:@@applicationLinkDeleteDialogConfirm:Confirma l'eliminació de l'enllaç`,
};

export const APPLICATION_LINK_DIALOG_TEXTS = {
  titles: {
    create: $localize`:@@applicationLinkDialogCreate:Afegeix un Enllaç`,
    view: $localize`:@@applicationLinkDialogView:Consulta de l'enllaç`,
    edit: $localize`:@@applicationLinkDialogEdit:Edició de l'enllaç`,
  },
  ariaLabels: {
    accept: $localize`:@@applicationLinkDialogAccept:Accepta la consulta de l'enllaç`,
    add: $localize`:@@applicationLinkDialogAdd:Afegeix l'enllaç`,
    cancel: $localize`:@@applicationLinkDialogCancel:Cancel·la els canvis de l'enllaç`,
    close: $localize`:@@applicationLinkDialogClose:Tanca el formulari de l'enllaç`,
    deactivate: $localize`:@@applicationLinkDialogDeactivate:Elimina l'enllaç`,
    edit: $localize`:@@applicationLinkDialogEditAction:Edita l'enllaç`,
    restore: $localize`:@@applicationLinkDialogRestore:Restaura l'enllaç`,
    save: $localize`:@@applicationLinkDialogSave:Desa els canvis de l'enllaç`,
  },
  labels: {
    title: $localize`:@@applicationLinkDialogTitleLabel:Títol`,
    description: $localize`:@@applicationLinkDialogDescription:Descripció (opcional)`,
    category: $localize`:@@applicationLinkDialogCategory:Categoria`,
    url: $localize`:@@applicationLinkDialogUrl:URL`,
  },
  requiredHint: $localize`:@@applicationLinkDialogRequiredHint:Tots els camps són obligatoris, excepte els indicats com a opcionals.`,
  actions: {
    cancel: $localize`:@@applicationLinkDialogCancelLabel:Cancel·la`,
    save: $localize`:@@applicationLinkDialogSaveLabel:Desa`,
  },
  urlError: $localize`:@@applicationLinkDialogUrlError:Indica una URL que comenci per http:// o https://`,
};

// Dialog that sends versions of the attachments of an application by e-mail.
export const APPLICATION_DOCUMENTATION_EMAIL_TEXTS = {
  title: $localize`:@@applicationDocumentationEmailTitle:Envia per correu`,
  openAriaLabel: $localize`:@@applicationDocumentationEmailOpen:Envia adjunts per correu`,
  requiredHint: $localize`:@@applicationDocumentationEmailRequiredHint:Tots els camps són obligatoris, excepte els indicats com a opcionals.`,
  labels: {
    recipients: $localize`:@@applicationDocumentationEmailRecipients:Destinataris`,
    subject: $localize`:@@applicationDocumentationEmailSubject:Assumpte`,
    message: $localize`:@@applicationDocumentationEmailMessage:Missatge (opcional)`,
  },
  recipientsHint: $localize`:@@applicationDocumentationEmailRecipientsHint:Escriu cada adreça i prem Retorn, espai o una coma per afegir-la. Fes doble clic en una adreça per canviar-la.`,
  recipientEditAriaLabel: (address: string) =>
    $localize`:@@applicationDocumentationEmailRecipientEdit:Canvia l'adreça ${address}:address:`,
  defaultSubject: (application: string) =>
    $localize`:@@applicationDocumentationEmailDefaultSubject:Documentació de l'aplicació ${application}:application:`,
  selectionTitle: $localize`:@@applicationDocumentationEmailSelectionTitle:Què vols enviar?`,
  selectedCount: (count: number) =>
    count === 1
      ? $localize`:@@applicationDocumentationEmailSelectedOne:1 element seleccionat`
      : $localize`:@@applicationDocumentationEmailSelectedMany:${count}:count: elements seleccionats`,
  quickSearchPlaceholder: $localize`:@@applicationDocumentationEmailQuickSearchPlaceholder:Cerca ràpida`,
  quickSearchAriaLabel: $localize`:@@applicationDocumentationEmailQuickSearchAriaLabel:Cerca adjunts per enviar pel títol`,
  columns: {
    selection: $localize`:@@applicationDocumentationEmailColumnSelection:Selecció`,
    document: $localize`:@@applicationDocumentationEmailColumnDocument:Document`,
    format: $localize`:@@applicationDocumentationEmailColumnFormat:Format`,
    version: $localize`:@@applicationDocumentationEmailColumnVersion:Versió`,
  },
  selectAriaLabel: (title: string) =>
    $localize`:@@applicationDocumentationEmailSelectDocument:Envia «${title}:title:»`,
  versionAriaLabel: (title: string) =>
    $localize`:@@applicationDocumentationEmailVersionOf:Versió de «${title}:title:» que s'envia`,
  empty: $localize`:@@applicationDocumentationEmailEmpty:No hi ha cap adjunt per enviar.`,
  errors: {
    recipientsRequired: $localize`:@@applicationDocumentationEmailRecipientsRequired:Indica com a mínim un destinatari.`,
    recipientsInvalid: (addresses: string) =>
      $localize`:@@applicationDocumentationEmailRecipientsInvalid:Aquestes adreces no són vàlides: ${addresses}:addresses:.`,
    recipientsTooMany: (max: number) =>
      $localize`:@@applicationDocumentationEmailRecipientsTooMany:No es pot enviar a més de ${max}:max: destinataris alhora.`,
    subjectLineBreak: $localize`:@@applicationDocumentationEmailSubjectLineBreak:L'assumpte no pot contenir salts de línia.`,
    attachmentsRequired: $localize`:@@applicationDocumentationEmailAttachmentsRequired:Selecciona com a mínim un adjunt.`,
    attachmentsTooLarge: $localize`:@@applicationDocumentationEmailAttachmentsTooLarge:Els adjunts seleccionats superen la mida màxima de 15 MB.`,
  },
  versionsLoadError: (title: string) =>
    $localize`:@@applicationDocumentationEmailVersionsLoadError:No s'han pogut carregar les versions de «${title}:title:»: s'enviarà la versió vigent.`,
  sentTitle: $localize`:@@applicationDocumentationEmailSentTitle:Correu enviat`,
  sentDetail: (count: number) =>
    count === 1
      ? $localize`:@@applicationDocumentationEmailSentOne:S'ha enviat el correu a 1 destinatari.`
      : $localize`:@@applicationDocumentationEmailSentMany:S'ha enviat el correu a ${count}:count: destinataris.`,
  sendError: $localize`:@@applicationDocumentationEmailSendError:No s'ha pogut enviar el correu.`,
  forbidden: $localize`:@@applicationDocumentationEmailForbidden:No tens permís per enviar algun dels adjunts seleccionats.`,
  actions: {
    cancel: $localize`:@@applicationDocumentationEmailCancelLabel:Cancel·la`,
    send: $localize`:@@applicationDocumentationEmailSendLabel:Envia`,
  },
  cancelAriaLabel: $localize`:@@applicationDocumentationEmailCancel:Cancel·la l'enviament per correu`,
  closeAriaLabel: $localize`:@@applicationDocumentationEmailClose:Tanca l'enviament per correu`,
};
