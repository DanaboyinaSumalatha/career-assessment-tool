import { useEffect, useState } from 'react';
import { adminService } from '../../services/adminService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import { Textarea } from '../../components/common/Input';
import toast from 'react-hot-toast';
import { Plus, Pencil, Trash2, Briefcase } from 'lucide-react';

const MOCK_CAREERS = [
  { id: 1, title: 'Software Engineer', industry: 'Technology', salaryRange: '$85k–$150k', growthRate: '25%', requiredSkills: 'Programming, Problem Solving', status: 'active' },
  { id: 2, title: 'Data Scientist', industry: 'Technology', salaryRange: '$90k–$160k', growthRate: '35%', requiredSkills: 'Python, Machine Learning', status: 'active' },
  { id: 3, title: 'UX Designer', industry: 'Creative', salaryRange: '$70k–$120k', growthRate: '13%', requiredSkills: 'Figma, User Research', status: 'active' },
  { id: 4, title: 'Product Manager', industry: 'Technology', salaryRange: '$100k–$180k', growthRate: '19%', requiredSkills: 'Leadership, Strategy', status: 'active' },
  { id: 5, title: 'Nurse Practitioner', industry: 'Healthcare', salaryRange: '$90k–$130k', growthRate: '40%', requiredSkills: 'Patient Care, Medical Knowledge', status: 'active' },
];

const EMPTY_FORM = { title: '', industry: '', salaryRange: '', growthRate: '', requiredSkills: '', description: '', education: '', workStyle: '', status: 'active' };

const CareerPathManagement = () => {
  const [careers, setCareers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState({ open: false, mode: 'add', data: null });
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const fetch = async () => {
      try {
        const { data } = await adminService.getAllCareerPaths();
        setCareers(data);
      } catch {
        setCareers(MOCK_CAREERS);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  const openAdd = () => { setForm(EMPTY_FORM); setModal({ open: true, mode: 'add' }); };
  const openEdit = (c) => { setForm(c); setModal({ open: true, mode: 'edit', data: c }); };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form.title.trim()) { toast.error('Title is required'); return; }
    setSaving(true);
    try {
      if (modal.mode === 'add') {
        await adminService.createCareerPath(form);
        setCareers((p) => [...p, { id: Date.now(), ...form }]);
        toast.success('Career path added!');
      } else {
        await adminService.updateCareerPath(modal.data.id, form);
        setCareers((p) => p.map((c) => c.id === modal.data.id ? { ...c, ...form } : c));
        toast.success('Career path updated!');
      }
      setModal({ open: false });
    } catch {
      toast.error('Failed to save.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this career path?')) return;
    try {
      await adminService.deleteCareerPath(id);
      setCareers((p) => p.filter((c) => c.id !== id));
      toast.success('Deleted.');
    } catch {
      toast.error('Failed to delete.');
    }
  };

  const columns = [
    { key: 'title', label: 'Career Title', render: (v) => <span className="font-medium text-gray-900">{v}</span> },
    { key: 'industry', label: 'Industry' },
    { key: 'salaryRange', label: 'Salary Range' },
    { key: 'growthRate', label: 'Growth' },
    { key: 'status', label: 'Status', render: (v) => <Badge variant={v === 'active' ? 'success' : 'default'}>{v}</Badge> },
    {
      key: 'actions', label: 'Actions',
      render: (_, row) => (
        <div className="flex gap-2">
          <button onClick={() => openEdit(row)} className="p-1.5 text-indigo-600 hover:bg-indigo-50 rounded-lg"><Pencil size={15} /></button>
          <button onClick={() => handleDelete(row.id)} className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg"><Trash2 size={15} /></button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader title="Career Path Management" subtitle="Manage available career paths for recommendations.">
        <Button onClick={openAdd}><Plus size={16} /> Add Career Path</Button>
      </PageHeader>

      <Table columns={columns} data={careers} loading={loading} />

      <Modal isOpen={modal.open} onClose={() => setModal({ open: false })} title={modal.mode === 'add' ? 'Add Career Path' : 'Edit Career Path'} size="lg">
        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid sm:grid-cols-2 gap-4">
            <Input label="Career Title" id="title" name="title" value={form.title} onChange={handleChange} required placeholder="e.g. Software Engineer" />
            <Input label="Industry" id="industry" name="industry" value={form.industry} onChange={handleChange} placeholder="e.g. Technology" />
          </div>
          <div className="grid sm:grid-cols-2 gap-4">
            <Input label="Salary Range" id="salaryRange" name="salaryRange" value={form.salaryRange} onChange={handleChange} placeholder="e.g. $80k–$120k" />
            <Input label="Growth Rate" id="growthRate" name="growthRate" value={form.growthRate} onChange={handleChange} placeholder="e.g. 25%" />
          </div>
          <div className="grid sm:grid-cols-2 gap-4">
            <Input label="Education Required" id="education" name="education" value={form.education} onChange={handleChange} placeholder="e.g. Bachelor's in CS" />
            <Input label="Work Style" id="workStyle" name="workStyle" value={form.workStyle} onChange={handleChange} placeholder="e.g. Remote/Hybrid" />
          </div>
          <Input label="Required Skills" id="requiredSkills" name="requiredSkills" value={form.requiredSkills} onChange={handleChange} placeholder="Comma-separated skills" />
          <Textarea label="Description" id="description" name="description" value={form.description} onChange={handleChange} rows={3} placeholder="Brief description of the career..." />

          <div className="flex gap-3 pt-2">
            <Button type="submit" loading={saving}>{modal.mode === 'add' ? 'Add Career Path' : 'Save Changes'}</Button>
            <Button type="button" variant="secondary" onClick={() => setModal({ open: false })}>Cancel</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default CareerPathManagement;
