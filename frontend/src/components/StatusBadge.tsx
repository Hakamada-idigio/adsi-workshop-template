interface StatusBadgeProps {
  status: "DRAFT" | "SUBMITTED" | "APPROVED" | "REJECTED";
}

const statusConfig = {
  DRAFT: { label: "下書き", className: "badge-draft" },
  SUBMITTED: { label: "提出済", className: "badge-submitted" },
  APPROVED: { label: "承認済", className: "badge-approved" },
  REJECTED: { label: "差戻し", className: "badge-rejected" },
};

export default function StatusBadge({ status }: StatusBadgeProps) {
  const config = statusConfig[status];
  return <span className={config.className}>{config.label}</span>;
}
